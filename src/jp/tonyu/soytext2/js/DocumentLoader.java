package jp.tonyu.soytext2.js;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.tonyu.db.NotInReadTransactionException;
import jp.tonyu.db.NotInWriteTransactionException;
import jp.tonyu.db.WriteAction;
import jp.tonyu.debug.Log;
import jp.tonyu.js.ContextRunnable;
import jp.tonyu.js.Scriptables;
import jp.tonyu.js.StringPropAction;
import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.auth.AuthenticatorList;
import jp.tonyu.soytext2.document.HashBlob;
import jp.tonyu.soytext2.document.IndexAction;
import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.document.DocumentAction;
import jp.tonyu.soytext2.document.DocumentSet;
import jp.tonyu.soytext2.document.IndexRecord;
import jp.tonyu.soytext2.document.LooseReadAction;
import jp.tonyu.soytext2.document.LooseTransaction;
import jp.tonyu.soytext2.document.LooseWriteAction;
import jp.tonyu.soytext2.document.PairSet;
import jp.tonyu.soytext2.document.UpdatingDocumentAction;
import jp.tonyu.soytext2.file.ReadableBinData;
import jp.tonyu.soytext2.search.Query;
import jp.tonyu.soytext2.search.QueryBuilder;
import jp.tonyu.soytext2.search.QueryResult;
import jp.tonyu.soytext2.search.QueryTemplate;
import jp.tonyu.soytext2.search.expr.AndExpr;
import jp.tonyu.soytext2.search.expr.AttrExpr;
import jp.tonyu.soytext2.search.expr.AttrOperator;
import jp.tonyu.soytext2.search.expr.BackLinkExpr;
import jp.tonyu.soytext2.search.expr.InstanceofExpr;
import jp.tonyu.soytext2.search.expr.QueryExpression;
import jp.tonyu.soytext2.servlet.Auth;
import jp.tonyu.soytext2.servlet.DocumentProcessor;
import jp.tonyu.soytext2.servlet.HttpContext;
import jp.tonyu.util.Ref;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

public class DocumentLoader implements Wrappable, IDocumentLoader {
    public static final jp.tonyu.util.Context<DocumentLoader> cur=new jp.tonyu.util.Context<DocumentLoader>();
    // private static final Object LOADING = "LOADING";
    public static final Pattern idpatWiki=DocumentProcessor.idpatWiki;// Pattern.compile("\\[\\[([^\\]]+)\\]\\]");
    private static final String ERROR_CONTENT="err_content";
    private static final String ERROR_MSG="err_message";
    // final LooseTransaction looseTransaction;
    // Map<String, Scriptable>objs=new HashMap<String, Scriptable>();
    private final DocumentSet documentSet;
    private Map<String, DocumentScriptable> objs=new HashMap<String, DocumentScriptable>();
    private final JSSession jsSession;
    public static WeakHashMap<DocumentLoader, Boolean> loaders=new WeakHashMap<DocumentLoader, Boolean>();
    final LooseTransaction ltr;
    public DocumentLoader(DocumentSet documentSet) {
        super();
        this.documentSet=Log.notNull(documentSet, "documentSet");
        // looseTransaction=new LooseTransaction(documentSet);
        ltr=new LooseTransaction(documentSet);
        this.jsSession=new JSSession();
        loaders.put(this, true);
    }
    public void notifySave(DocumentRecord d) {
        for (DocumentLoader dl : loaders.keySet()) {
            if (dl!=this) {
                dl.onSaveNotified(d);
            }
        }
    }
    public void onSaveNotified(DocumentRecord d) {
        DocumentScriptable s=objs.get(d.id);
        if (s!=null) {
            s.reloadFromContent();
        }
    }
    /*
     * private Scriptable instanciator(final DocumentRecord src) { return new
     * BlankScriptableObject() { private static final long serialVersionUID =
     * -3858849843957575405L;
     *
     * @Override public Object get(String name, Scriptable start) { if
     * ("create".equals(name)) { // src ! create. (in dolittle) return new
     * BuiltinFunc() {
     *
     * @Override public Object call(Context cx, Scriptable scope, Scriptable
     * thisObj, Object[] args) { DocumentScriptable res =
     * DocumentLoader.this.defaultDocumentScriptable(src); if (args.length>=1) {
     * res.setPrototype(byId(args[0]+"")); } return res; } }; } if
     * ("newInstance".equals(name)) { // new func() return new BuiltinFunc() {
     *
     * @Override public Object call(Context cx, Scriptable scope, Scriptable
     * thisObj, Object[] args) { DocumentScriptable res =
     * DocumentLoader.this.defaultDocumentScriptable(src); if (args.length>=1) {
     * DocumentScriptable func = byId(args[0]+""); Object proto =
     * func.get("prototype"); if (proto instanceof Scriptable) { Scriptable sc =
     * (Scriptable) proto; res.setPrototype(sc); } } return res; } }; }
     *
     * Log.die(name+" not found "); return super.get(name, start); } }; }
     */
    /*
     * $.byId(id) returns some DocumentScriptable even if it is not exist.
         it is for lazy loading of DocumentRecord to avoid much queries.
     */
    public DocumentScriptable byId(final String id) {
        DocumentScriptable o=objs.get(id);
        if (o!=null)
            return o;
        return defaultDocumentScriptable(id);
    }
    public DocumentScriptable byIdOrNull(final String id) {
        DocumentScriptable o=objs.get(id);
        if (o!=null)
            return o;
        DocumentRecord r=recordById(id);
        if (r==null) return null;
        return defaultDocumentScriptable(r);
    }
    public DocumentRecord recordById(final String id) {
        final Ref<DocumentRecord> src=Ref.create(null);
        ltr.read(new LooseReadAction() {
            @Override
            public void run() throws NotInReadTransactionException {
                src.set(Log.notNull(getDocumentSet(), "gds").byId(id));
            }
        });
        return src.get();
    }
    private DocumentScriptable byRecordOrCache(final DocumentRecord src) {
        DocumentScriptable o=objs.get(src.id);
        if (o!=null) {
            if (!o.isRecordLoaded()) {// It is needed for fullTextGrep
                o.loadRecord(src);
            }
            return o;
        }
        return byRecord(src);
    }
    private DocumentScriptable byRecord(final DocumentRecord src) {
        DocumentScriptable o;
        // if (src.preContent==null || src.preContent.trim().length()==0) {
        o=defaultDocumentScriptable(src);
        /*
         * } else { try { Scriptable inst=instanciator(src);
         * o=(DocumentScriptable)jsSession().eval("preLoad:"+id,src.preContent,
         * Maps.create("$", (Object)inst)); } catch(Exception e) {
         * e.printStackTrace(); Log.d(this,
         * "Instanciation error - "+src.preContent);
         * o=defaultDocumentScriptable(src); } }
         */
        // objs.put(id, o); moved to defDocscr
        if (src.content!=null) {
            if (DocumentScriptable.lazyLoad==false) {
                ind.append(" ");
                Log.d(this, "DLoader.loadFromContent"+ ind+"["+src.id+"]");// "+src.content);
                loadFromContent(src.content, o);
                ind.delete(0, 1);
            }
        } else {
            Log.d(this, src.id+".content is still null");
            /*
             * Why allowed this? at 1206@1.2010.tonyu.jp com=new Comment();
             * wrt(com); sub.com=com; sub.save(); // At this time, com.content
             * == null. // And notify content of sub to other sessions // In
             * other sessions, com has not loaded(because it is new) // Thus,
             * com will loaded while saving sub with null content com.save(); //
             * at this time, com.content is properly set. No problem.
             */
        }
        return o;
    }
    /*public DocumentScriptable reloadFromRecord(String id) {
        DocumentScriptable res=objs.get(id);
        if (res==null)
            return byIdOrNull(id);
        res.setContentAndSave(res.getDocument().content);
        return res;
    }*/
    public void save(final DocumentRecord d, final PairSet<String, String> updatingIndex) {
        if (d.content==null)
            Log.die("Content of "+d.id+" is null!");
        ltr.write(new LooseWriteAction() {
            @Override
            public void run() throws NotInWriteTransactionException {
                getDocumentSet().save(d, updatingIndex);// d.save();
            }
        });
        notifySave(d);
    }
    // Map<String, DocumentScriptable> debugH=new HashMap<String,
    // DocumentScriptable>();
    private DocumentScriptable defaultDocumentScriptable(final DocumentRecord src) {
        DocumentScriptable res=new DocumentScriptable(this, src);
        if (objs.containsKey(src.id))
            Log.die("Already have "+src);
        objs.put(src.id, res);
        return res;
    }
    private DocumentScriptable defaultDocumentScriptable(final String id) {
        DocumentScriptable res=new DocumentScriptable(this, id);
        if (objs.containsKey(id))
            Log.die("Already have "+id);
        objs.put(id, res);
        return res;
    }
    static StringBuffer ind=new StringBuffer();
    public void loadFromContent(final String newContent, DocumentScriptable dst) {
        if (newContent==null)
            Log.die("New content is null!");
        dst.clear();
        DocumentLoaderScriptable loaderScope=new DocumentLoaderScriptable(jsSession().root, this, dst);
        try {
            jsSession().eval(dst+"", newContent, loaderScope);
            dst.put(HttpContext.ATTR_SCOPE, loaderScope.scope());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(e);
            Log.d(this, dst.id()+" has invalid content "+newContent);
            dst.put(ERROR_MSG, e.getMessage());
            dst.put(ERROR_CONTENT, newContent);
        }
    }
    public HashBlob hashBlob(String hash) {
        //ReadableBinData b=documentSet.getHashBlob(hash);
        return documentSet.getHashBlob(hash);
    }
    public HashBlob writeHashBlob(ReadableBinData i) throws IOException {
        return writeHashBlob(i.getInputStream());
    }
    public HashBlob writeHashBlob(InputStream i) {
        HashBlob r=documentSet.writeHashBlob(i);
        return r;
    }
    public static JSSession curJsSesssion() {
        return cur.get().jsSession();
    }
    public JSSession jsSession() {
        return jsSession; // JSSession.cur.get();
    }
    public DocumentScriptable newDocument(final String id) {
        final Ref<DocumentScriptable> res=Ref.create(null);
        ltr.write(new LooseWriteAction() {
            @Override
            public void run() throws NotInWriteTransactionException {
                DocumentRecord d=getDocumentSet().newDocument(id);
                d.owner=Auth.cur.get().user();
                res.set(defaultDocumentScriptable(d));
            }
        });
        return res.get();
    }
    public DocumentScriptable newDocument() {
        final Ref<DocumentScriptable> res=Ref.create(null);
        ltr.write(new LooseWriteAction() {
            @Override
            public void run() throws NotInWriteTransactionException {
                DocumentRecord d=getDocumentSet().newDocument();
                d.owner=Auth.cur.get().user();
                res.set(defaultDocumentScriptable(d));
            }
        });
        return res.get();
    }
    public DocumentScriptable newDocument(Scriptable hash) {
        final Object id=hash!=null ? hash.get("id", hash) : null;
        final Ref<DocumentRecord> d=Ref.create(null);
        ltr.write(new LooseWriteAction() {
            @Override
            public void run() throws NotInWriteTransactionException {
                if (id instanceof String) {
                    d.set(getDocumentSet().newDocument((String) id));
                } else {
                    d.set(getDocumentSet().newDocument());
                }
            }
        });
        final DocumentScriptable res=defaultDocumentScriptable(d.get());
        extend(res, hash);
        return res;
    }
    public void search(String cond, Scriptable tmpl, final Function iter) {
        final Query q=newQuery(cond, tmpl);
        searchByQuery(q, iter);
    }
    /*
     * private Map<String, String> extractIndexExpr(QueryExpression e) throws
     * NotInReadTransactionException { final Map<String, String> idxs=new
     * HashMap<String, String>(); extractIndexExpr(idxs, e); return idxs; }
     */
    private void extractIndexExpr(Map<String, String> idxs, QueryExpression e) throws NotInReadTransactionException {
        if (e instanceof AndExpr) {
            AndExpr a=(AndExpr) e;
            for (QueryExpression ea : a) {
                extractIndexExpr(idxs, ea);
                if (!idxs.isEmpty())
                    break; // get only first: the result of
                           // first cond must be fewer than
                           // following
                // QueryExpression res=extractIndexExpr(idxs,ea);
                // if (res!=null) return res;
            }
        } else if (e instanceof AttrExpr) {
            AttrExpr aidx=(AttrExpr) e;
            String key=aidx.getKey();
            Object value=aidx.getValue();
            if (getDocumentSet().indexAvailable(key)) {
                if (value instanceof DocumentScriptable) {
                    DocumentScriptable ds=(DocumentScriptable) value;
                    value=ds.getDocument().id;
                }
                idxs.put(key, value.toString());
            } else {
                if (value instanceof DocumentScriptable) {
                    DocumentScriptable ds=(DocumentScriptable) value;
                    value=ds.getDocument().id;
                    idxs.put(IndexRecord.INDEX_REFERS, value.toString());
                }
            }
        } else if (e instanceof BackLinkExpr) {
            BackLinkExpr bidx=(BackLinkExpr) e;
            idxs.put(IndexRecord.INDEX_REFERS, bidx.toId);
            // return b;
        } else if (e instanceof InstanceofExpr) {
            InstanceofExpr iidx=(InstanceofExpr) e;
            idxs.put(IndexRecord.INDEX_INSTANCEOF, iidx.klass);
        }
        // return null;
    }
    private Map<String, String> extractIdxMap(Query q) {
        final QueryTemplate qt=q.getTemplate();
        final QueryExpression e=qt.getCond();
        Log.d(this, "Search by "+q);
        final Map<String, String> idxs=new HashMap<String, String>();
        ltr.read(new LooseReadAction() {
            public void run() throws NotInReadTransactionException {
                extractIndexExpr(idxs, e);
            }
        });
        return idxs;
    }
    public void searchByQuery(final Query q, final Function iter) {
        final Map<String, String> idxs=extractIdxMap(q);
        if (idxs.size()==0) {
            ltr.read(new LooseReadAction() {
                @Override
                public void run() throws NotInReadTransactionException {
                    Log.d(this, "Search add");
                    DocumentAction docAct=new DocumentAction() {
                        @Override
                        public boolean run(DocumentRecord d) throws NotInReadTransactionException {
                            DocumentScriptable s=byRecordOrCache(d);
                            return callDocIter(s , q , iter);
                        }
                    };
                    getDocumentSet().all(docAct);
                }
            });
        } else {
            ltr.read(new LooseReadAction() {
                @Override
                public void run() throws NotInReadTransactionException {
                    Log.d(this, "Search with index "+idxs);
                    IndexAction docAct=new IndexAction() {
                        @Override
                        public boolean run(IndexRecord i) {
                            Log.d("Index Matched", i.document);
                            DocumentScriptable s=(DocumentScriptable) byIdOrNull(i.document);
                            return callDocIter(s , q , iter);
                        }
                    };
                    getDocumentSet().searchByIndex(idxs, docAct);
                }
            });
        }
    }
    private boolean callDocIter(DocumentScriptable s, final Query q, final Function iter) {
        QueryResult r=q.matches(s);
        if (r.filterMatched) {
            Log.d(this, "Filter matched1 "+s);
            Object brk=null;
            //try {
                brk=jsSession().call(iter, iter, new Object[] { s });
            /*} catch(Throwable e) {
                Log.d(this, "Error!?");
                e.printStackTrace();
                throw new RuntimeException(e);
            }*/
            Log.d(this, "Filter matched2 "+s);
            if (brk instanceof Boolean) {
                Boolean b=(Boolean) brk;
                if (b.booleanValue())
                    return true;
            }
        }
        return false;
    }
    public void updatingSearchByQuery(final Query q, final Function iter) {
        ltr.write(new LooseWriteAction() {
            @Override
            public void run() throws NotInWriteTransactionException {
                searchByQuery(q, iter);
            }
        });
    }
    public Query newQuery(String cond, Scriptable tmpl) {
        final QueryBuilder qb=QueryBuilder.create(cond);
        if (tmpl!=null) {
            Scriptables.each(tmpl, new StringPropAction() {
                @Override
                public void run(String key, Object value) {
                    AttrOperator op=AttrOperator.ge;
                    if (value instanceof String) {
                        String svalue=(String) value;
                        if (svalue.startsWith("=")) {
                            op=AttrOperator.exact;
                            value=svalue.substring(1);
                        }
                    }
                    qb.tmpl(key, value, op);
                }
            });
            /*
             * for (Object n:tmpl.getIds()) { if (n instanceof String) { String
             * name = (String) n; Object value=tmpl.get(name, tmpl);
             * AttrOperator op=AttrOperator.ge; if (value instanceof String) {
             * String svalue = (String) value; if (svalue.startsWith("=")) {
             * op=AttrOperator.exact; value=svalue.substring(1); } }
             * qb.tmpl(name, value, op); } }
             */
        }
        final Query q=qb.toQuery();
        return q;
    }
    /*
     * (non-Javadoc)
     *
     * @see jp.tonyu.soytext2.js.IDocumentLoader#extend(jp.tonyu.soytext2.js.
     * DocumentScriptable, org.mozilla.javascript.Scriptable)
     */
    public void extend(final DocumentScriptable dst, Scriptable src) {
        if (src==null)
            return;
        Scriptables.each(src, new StringPropAction() {
            @Override
            public void run(String key, Object value) {
                String str=(String) key;
                Matcher m=idpatWiki.matcher(str);
                if (m.matches()) {
                    String id=m.group(1);
                    DocumentScriptable refd=byIdOrNull(id);
                    if (refd==null)
                        Log.die("[["+id+"]] not found");
                    dst.put(refd, value);
                } else {
                    dst.put(key, value);
                }
            }
        });
        /*
         * for (Object key:hash.getIds()) { if (key instanceof String) { String
         * str = (String) key; Matcher m=idpatWiki.matcher(str); Object value =
         * hash.get(str, null); if (m.matches()) { String id=m.group(1);
         * //Log.d(this, "Put "+dst.getDocument().id+" . "+id+" = "+value);
         * DocumentScriptable refd = byId(id); if (refd==null)
         * Log.die("[["+id+"]] not found"); dst.put(refd, value); } else {
         * dst.put(key, value); } } }
         */
    }
    public void setGetter(DocumentScriptable dst, final Function func) {
        dst.setGetter(new Getter() {
            @Override
            public Object getFrom(Object src) {
                return jsSession().call(func, new Object[] { src });
            }
        });
    }
    @Override
    public Wrappable javaNative(String className) {
        try {
            return (Wrappable) Class.forName(className).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    public DocumentSet getDocumentSet() {
        return documentSet;
    }
    @Override
    public Scriptable inherit(Function superclass, Scriptable overrideMethods) {
        BlessedScriptable res=new BlessedScriptable(superclass);
        Scriptables.extend(res, overrideMethods);
        return res;
    }
    @Override
    public Scriptable bless(Function klass, Scriptable fields) {
        return inherit(klass, fields);
    }
    public DocumentScriptable rootDocument() {
        return byIdOrNull(rootDocumentId());
    }
    public String rootDocumentId() {
        return "root@"+documentSet.getDBID();  // TODO: @.
    }
    AuthenticatorList auth;
    public AuthenticatorList authenticator() {
        if (auth!=null)
            return auth;
        auth=new AuthenticatorList();
        DocumentScriptable r=rootDocument();
        Object a=r.get("authenticator");
        if (a instanceof Function) {
            Function f=(Function) a;
            Log.d(this, "Using - "+f+" as authlist");
            jsSession.call(f, new Object[] { auth });
        }
        /*
         * QueryBuilder qb=QueryBuilder.create("authenticatorList:true");
         * searchByQuery(qb.toQuery(), new BuiltinFunc() {
         *
         * @Override public Object call(Context cx, Scriptable scope, Scriptable
         * thisObj, Object[] args) { Function f=(Function) args[0]; Log.d(this,
         * "Using - "+f+" as authlist"); f.call(cx, scope, f, new
         * Object[]{auth}); return true; } });
         */
        return auth;
    }
    private void copyDocumentExceptDates(DocumentRecord src, DocumentRecord dst) throws SQLException {
        long lu=dst.lastUpdate;
        src.copyTo(dst);
        dst.lastUpdate=lu;
    }
    /**
     *
     * @param drs
     *            DocumentRecords to be imported
     * @throws SQLException
     * @throws NotInWriteTransactionException
     */
    public void importDocuments(Collection<DocumentRecord> drs) throws SQLException, NotInWriteTransactionException {
        Set<DocumentScriptable> willReload=new HashSet<DocumentScriptable>();
        Set<String> willUpdateIndex=new HashSet<String>();
        for (DocumentRecord dr : drs) {
            DocumentRecord existentDr=documentSet.byId(dr.id);
            if (existentDr!=null) {
                copyDocumentExceptDates(dr, existentDr);
                /* documentSet. */save(existentDr, new PairSet<String, String>());
            } else {
                DocumentRecord newDr=getDocumentSet().newDocument(dr.id);
                copyDocumentExceptDates(dr, newDr);
                /* documentSet. */save(newDr, new PairSet<String, String>());
            }
            willUpdateIndex.add(dr.id);
            if (objs.containsKey(dr.id))
                willReload.add(objs.get(dr.id));
        }
        for (DocumentScriptable ds : willReload) {
            ds.reloadFromContent();
        }
        // if (((SDB)documentSet).useIndex()) {
        for (String id : willUpdateIndex) {
            DocumentScriptable s=byIdOrNull(id);
            s.refreshIndex();
        }
        // }
    }
    public void rebuildIndex() {
        JSSession.withContext(new ContextRunnable() {
            @Override
            public Object run(Context cx) {
                ltr.write(new LooseWriteAction() {
                    @Override
                    public void run() throws NotInWriteTransactionException {
                        documentSet.all(new UpdatingDocumentAction() {
                            @Override
                            public boolean run(DocumentRecord d) throws NotInWriteTransactionException {
                                Log.d("rebuildIndex", d.id);// +" lastUpdate="+d.lastUpdate);
                                DocumentScriptable s=byRecordOrCache(d);
                                s.refreshIndex();
                                return false;
                            }
                        });
                    }
                });
                /*
                 * documentSet.all(new DocumentAction() {
                 *
                 * @Override public boolean run(final DocumentRecord d) {
                 * documentSet.transaction("write", new Runnable() {
                 *
                 * @Override public void run() {
                 * Log.d("rebuildIndex",d.id);//+" lastUpdate="+d.lastUpdate);
                 * DocumentScriptable s=byRecordOrCache(d); s.refreshIndex(); }
                 * }); return true; } });
                 */
                return null;
            }
        });
    }
}
