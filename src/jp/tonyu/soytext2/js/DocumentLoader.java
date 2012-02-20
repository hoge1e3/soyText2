package jp.tonyu.soytext2.js;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.tonyu.debug.Log;
import jp.tonyu.js.BuiltinFunc;
import jp.tonyu.js.ContextRunnable;
import jp.tonyu.js.Scriptables;
import jp.tonyu.js.StringPropAction;
import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.auth.AuthenticatorList;
import jp.tonyu.soytext2.document.DocumentAction;
import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.document.DocumentSet;
import jp.tonyu.soytext2.document.IndexRecord;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.document.PairSet;
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
import jp.tonyu.soytext2.servlet.DocumentProcessor;
import jp.tonyu.soytext2.servlet.HttpContext;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.tmatesoft.sqljet.core.SqlJetException;


public class DocumentLoader implements Wrappable, IDocumentLoader {
	public static final jp.tonyu.util.Context<DocumentLoader> cur=new jp.tonyu.util.Context<DocumentLoader>();
	//private static final Object LOADING = "LOADING";
	public static final Pattern idpatWiki= DocumentProcessor.idpatWiki ;//Pattern.compile("\\[\\[([^\\]]+)\\]\\]");
	private static final String ERROR_CONTENT = "err_content";
	private static final String ERROR_MSG = "err_message";
	//Map<String, Scriptable>objs=new HashMap<String, Scriptable>();
	private final DocumentSet documentSet;
	private Map<String, DocumentScriptable> objs=new HashMap<String, DocumentScriptable>();
	private final JSSession jsSession;
	public static WeakHashMap<DocumentLoader,Boolean> loaders=new WeakHashMap<DocumentLoader,Boolean>();
	public DocumentLoader(DocumentSet documentSet) {
		super();
		this.documentSet = Log.notNull(documentSet,"documentSet");
		
		this.jsSession=new JSSession();
		loaders.put(this,true);
	}
	public void notifySave(DocumentRecord d) {
		for (DocumentLoader dl:loaders.keySet()) {
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
	/*private Scriptable instanciator(final DocumentRecord src) {
		return new BlankScriptableObject() {
			private static final long serialVersionUID = -3858849843957575405L;

			@Override
			public Object get(String name, Scriptable start) {
				if ("create".equals(name)) { //   src ! create.  (in dolittle)
					return new BuiltinFunc() {

						@Override
						public Object call(Context cx, Scriptable scope, Scriptable thisObj,
								Object[] args) {
							DocumentScriptable res = DocumentLoader.this.defaultDocumentScriptable(src);
							if (args.length>=1) {
								res.setPrototype(byId(args[0]+""));
							}
							return res;
						}
					};
				}
				if ("newInstance".equals(name)) { //  new func() 
					return new BuiltinFunc() {

						@Override
						public Object call(Context cx, Scriptable scope, Scriptable thisObj,
								Object[] args) {
							DocumentScriptable res = DocumentLoader.this.defaultDocumentScriptable(src);
							if (args.length>=1) {
								DocumentScriptable func = byId(args[0]+"");
								Object proto = func.get("prototype");
								if (proto instanceof Scriptable) {
									Scriptable sc = (Scriptable) proto;
									res.setPrototype(sc);
								}
							}
							return res;
						}
					};
				}

				Log.die(name+" not found ");
				return super.get(name, start);
			}
		};
	}*/
	/* (non-Javadoc)
	 * @see jp.tonyu.soytext2.js.IDocumentLoader#byId(java.lang.String)
	 */
	public DocumentScriptable byId(String id) {
		final DocumentRecord src=Log.notNull(getDocumentSet(),"gds").byId(id);

		if (src==null) return null;
		DocumentScriptable o=objs.get(id);
		if (o!=null) return o;
		//if (src.preContent==null || src.preContent.trim().length()==0) {
			o=defaultDocumentScriptable(src);
		/*} else {
			try {
				Scriptable inst=instanciator(src);
				o=(DocumentScriptable)jsSession().eval("preLoad:"+id,src.preContent, Maps.create("$", (Object)inst));
			} catch(Exception e) {
				e.printStackTrace();
				Log.d(this, "Instanciation error - "+src.preContent);
				o=defaultDocumentScriptable(src);
			}
		}*/
		//objs.put(id, o); moved to defDocscr
		if (src.content!=null) {
			loadFromContent(src.content, o);
		} else {
			Log.d(this, src.id+".content is still null");
			/*
			 * Why allowed this?
			 *  at  1206@1.2010.tonyu.jp
			   com=new Comment();
               wrt(com);
               sub.com=com;
               sub.save(); // At this time, com.content == null. 
                           // And notify content of sub to other sessions
                           // In other sessions, com has not loaded(because it is new)
                           // Thus, com will loaded while saving sub with null content                           
               com.save(); // at this time, com.content is properly set. No problem.

			 */
		}
		return o;

	}
	public DocumentScriptable reload(String id) {
		DocumentScriptable res=objs.get(id);
		if (res==null) return byId(id);
		res.setContentAndSave(res.getDocument().content);
		return res;
	}
	public void save(DocumentRecord d,PairSet<String,String> updatingIndex) {
		if (d.content==null) Log.die("Content of "+d.id+" is null!");
		getDocumentSet().save(d,updatingIndex);// d.save();
		notifySave(d);
	}
	//Map<String, DocumentScriptable> debugH=new HashMap<String, DocumentScriptable>();

	private DocumentScriptable defaultDocumentScriptable(final DocumentRecord src) {
		DocumentScriptable res = new DocumentScriptable(this, src);
		if (objs.containsKey(src.id)) Log.die("Already have "+src);
		
		objs.put(src.id, res);
		return res;
	}
	public void loadFromContent(final String newContent, DocumentScriptable dst) {
		if (newContent==null) Log.die("New content is null!");
		//BlankScriptableObject tools=new BlankScriptableObject(jsSession().root);
		dst.clear();
		//tools.put("$", this);
		//tools.put("_", dst);
		//BlankScriptableObject scope = new BlankScriptableObject(jsSession().root);
		//scope.setPrototype(tools);
		DocumentLoaderScriptable loaderScope = new DocumentLoaderScriptable(jsSession().root, this, dst);
		try {
			jsSession().eval(dst.getDocument()+"", newContent, loaderScope);
			dst.put(HttpContext.ATTR_SCOPE, loaderScope.scope());
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(e);
			Log.d(this , dst.getDocument().id+" has invalid content "+newContent);
			dst.put(ERROR_MSG, e.getMessage() );
			dst.put(ERROR_CONTENT, newContent );
		}
	}
	public static JSSession curJsSesssion() {
		return cur.get().jsSession();
	}
	public JSSession jsSession() {
		return jsSession; //JSSession.cur.get();
	}
	public DocumentScriptable newDocument(String id) {
		DocumentRecord d = getDocumentSet().newDocument((String)id);
		final DocumentScriptable res=defaultDocumentScriptable(d);
		return res;
	}
	public DocumentScriptable newDocument() {
		DocumentRecord d = getDocumentSet().newDocument();
		final DocumentScriptable res=defaultDocumentScriptable(d);
		return res;
	}
	public DocumentScriptable newDocument(Scriptable hash) {
		final Object id = hash!=null ? hash.get("id", hash) : null;
		final DocumentRecord d;
		if (id instanceof String) {
			d=getDocumentSet().newDocument((String)id);
		} else {
			d=getDocumentSet().newDocument();
		}
		final DocumentScriptable res=defaultDocumentScriptable(d);
		extend(res,hash);
		return res;
	}
	public void search(String cond, Scriptable tmpl, final Function iter) {
		final Query q = newQuery(cond, tmpl);
		searchByQuery(q,iter);
	}
	private QueryExpression extractIndexExpr(QueryExpression e) {
		if (e instanceof AndExpr) {
			AndExpr a = (AndExpr) e;
			for (QueryExpression ea:a) {
				QueryExpression res=extractIndexExpr(ea);
				if (res!=null) return res;
			}
		} else if (e instanceof AttrExpr) {
			AttrExpr r = (AttrExpr) e;
			if (getDocumentSet().indexAvailable(r.getKey())) {
				return r;
			}
		} else if (e instanceof BackLinkExpr) {
			BackLinkExpr b = (BackLinkExpr) e;
			return b;
		} else if (e instanceof InstanceofExpr) {
			InstanceofExpr i = (InstanceofExpr) e;
			return i;
		}
		return null;
	}
	public void searchByQuery(final Query q, final Function iter) {
		Log.d(this, "Search by "+q);
		QueryTemplate qt = q.getTemplate();
		QueryExpression e = qt.getCond();
		QueryExpression idx = extractIndexExpr(e);
		DocumentAction docAct = new DocumentAction() {

			@Override
			public boolean run(DocumentRecord d) {
				Log.d("QueryMatched", d);
				DocumentScriptable s=(DocumentScriptable) byId(d.id);
				QueryResult r = q.matches(s);
				if (r.filterMatched) {
					Object brk=jsSession().call(iter, iter, new Object[]{s});
					if (brk instanceof Boolean) {
						Boolean b = (Boolean) brk;
						if (b.booleanValue()) return true;
					}
				}
				return false;
			}
		};
		if (idx==null) {
			getDocumentSet().all(docAct);			
		} else {
			if (idx instanceof AttrExpr) {
				AttrExpr aidx = (AttrExpr) idx;
				Object value = aidx.getValue();
				if (value instanceof DocumentScriptable) {
					DocumentScriptable ds = (DocumentScriptable) value;
					value=ds.getDocument().id;
				}
				getDocumentSet().searchByIndex(aidx.getKey(), value.toString(), docAct);
			} else if (idx instanceof BackLinkExpr) {
				BackLinkExpr bidx = (BackLinkExpr) idx;
				getDocumentSet().searchByIndex(IndexRecord.INDEX_REFERS, bidx.toId, docAct);
			}  else if (idx instanceof InstanceofExpr) {
				InstanceofExpr iidx = (InstanceofExpr) idx;
				getDocumentSet().searchByIndex(IndexRecord.INDEX_INSTANCEOF, iidx.klass, docAct);
			}
		}
	}
	public Query newQuery(String cond, Scriptable tmpl) {
		final QueryBuilder qb=QueryBuilder.create(cond);
		if (tmpl!=null) {
			Scriptables.each(tmpl, new StringPropAction() {
				@Override
				public void run(String key, Object value) {
					AttrOperator op=AttrOperator.ge;
					if (value instanceof String) {
						String svalue = (String) value;
						if (svalue.startsWith("=")) {
							op=AttrOperator.exact;
							value=svalue.substring(1);
						}
					}
					qb.tmpl(key, value, op);				
				}
			});
			/*for (Object n:tmpl.getIds()) {
				if (n instanceof String) {
					String name = (String) n;
					Object value=tmpl.get(name, tmpl);
					AttrOperator op=AttrOperator.ge;
					if (value instanceof String) {
						String svalue = (String) value;
						if (svalue.startsWith("=")) {
							op=AttrOperator.exact;
							value=svalue.substring(1);
						}
					}
					qb.tmpl(name, value, op);				
				}
			}*/
		}
		final Query q=qb.toQuery();
		return q;
	}
	/* (non-Javadoc)
	 * @see jp.tonyu.soytext2.js.IDocumentLoader#extend(jp.tonyu.soytext2.js.DocumentScriptable, org.mozilla.javascript.Scriptable)
	 */
	public void extend(final DocumentScriptable dst, Scriptable src) {
		if (src==null) return;
		Scriptables.each(src, new StringPropAction() {
			@Override
			public void run(String key, Object value) {
				String str = (String) key;
				Matcher m=idpatWiki.matcher(str);
				if (m.matches()) {
					String id=m.group(1);
					DocumentScriptable refd = byId(id);
					if (refd==null) Log.die("[["+id+"]] not found");
					dst.put(refd, value);
				} else {
					dst.put(key, value);
				}
			}
		});
		/*for (Object key:hash.getIds()) {
			if (key instanceof String) {
				String str = (String) key;
				Matcher m=idpatWiki.matcher(str);
				Object value = hash.get(str, null);
				if (m.matches()) {
					String id=m.group(1);
					//Log.d(this, "Put "+dst.getDocument().id+" . "+id+" = "+value);
					DocumentScriptable refd = byId(id);
					if (refd==null) Log.die("[["+id+"]] not found");
					dst.put(refd, value);
				} else {
					dst.put(key, value);
				}
			}
		}*/
	}
	public void setGetter(DocumentScriptable dst, final Function func) {
		dst.setGetter(new Getter() {

			@Override
			public Object getFrom(Object src) {
				return jsSession().call(func, new Object[]{src});
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
		BlessedScriptable res = new BlessedScriptable(superclass);
		Scriptables.extend(res, overrideMethods);
		return res;
	}
	@Override
	public Scriptable bless(Function klass, Scriptable fields) {
		return inherit(klass, fields);
	}
	AuthenticatorList auth;
	public AuthenticatorList authenticator() {
		if (auth!=null) return auth;
		auth=new AuthenticatorList();
		QueryBuilder qb=QueryBuilder.create("authenticatorList:true");
		searchByQuery(qb.toQuery(), new BuiltinFunc() {
			
			@Override
			public Object call(Context cx, Scriptable scope, Scriptable thisObj,
					Object[] args) {
				Function f=(Function) args[0];
				Log.d(this, "Using - "+f+" as authlist");
				f.call(cx, scope, f, new Object[]{auth});
				return true;
			}
		});
		return auth;
	}
	private void copyDocumentExceptDates(DocumentRecord src, DocumentRecord dst) throws SqlJetException {
		long lu=dst.lastUpdate;
		src.copyTo(dst);
		dst.lastUpdate=lu;
	}
	/**
	 * 
	 * @param dr DocumentRecord to be imported
	 * @return true if DocumentScriptable having id equals to dr.id in objs(cache)
	 * @throws SqlJetException
	 */
	public void importDocuments(Collection<DocumentRecord> drs) throws SqlJetException {
		Set<DocumentScriptable> willReload=new HashSet<DocumentScriptable>();
		Set<String> willUpdateIndex=new HashSet<String>();
		for (DocumentRecord dr:drs) {
			DocumentRecord existentDr=documentSet.byId(dr.id);
			if (existentDr!=null) {
				copyDocumentExceptDates(dr , existentDr);
				/*documentSet.*/save(existentDr, new PairSet<String,String>());
			} else {
				DocumentRecord newDr=getDocumentSet().newDocument(dr.id);
				copyDocumentExceptDates(dr, newDr);
				/*documentSet.*/save(newDr, new PairSet<String,String>());
			}
			willUpdateIndex.add(dr.id);
			if (objs.containsKey(dr.id)) willReload.add(objs.get(dr.id));
		}
		for (DocumentScriptable ds:willReload) {
			ds.reloadFromContent();
		}
		if (((SDB)documentSet).useIndex()) {
			for (String id: willUpdateIndex) {
				DocumentScriptable s=byId(id);
				s.refreshIndex();
			}			
		}
	}
	public void rebuildIndex() {
		JSSession.withContext(new ContextRunnable() {
			
			@Override
			public Object run(Context cx) {
				documentSet.all(new DocumentAction() {
					
					@Override
					public boolean run(DocumentRecord d) {
						DocumentScriptable s=byId(d.id);
						s.refreshIndex();
						return false;
					}
				});
				return null;
			}
		});
	}
}
