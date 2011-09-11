package jp.tonyu.soytext2.js;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.tonyu.debug.Log;
import jp.tonyu.js.BlankScriptableObject;
import jp.tonyu.js.BuiltinFunc;
import jp.tonyu.js.Scriptables;
import jp.tonyu.js.StringPropAction;
import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.auth.Authenticator;
import jp.tonyu.soytext2.auth.AuthenticatorList;
import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.document.DocumentAction;
import jp.tonyu.soytext2.document.DocumentSet;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.search.Query;
import jp.tonyu.soytext2.search.QueryBuilder;
import jp.tonyu.soytext2.search.QueryResult;
import jp.tonyu.soytext2.search.expr.AttrOperator;
import jp.tonyu.soytext2.servlet.DocumentProcessor;
import jp.tonyu.soytext2.servlet.HttpContext;
import jp.tonyu.util.Maps;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.tmatesoft.sqljet.core.SqlJetException;


public class DocumentLoader implements Wrappable, IDocumentLoader {
	//private static final Object LOADING = "LOADING";
	public static final Pattern idpatWiki= DocumentProcessor.idpatWiki ;//Pattern.compile("\\[\\[([^\\]]+)\\]\\]");
	private static final String ERROR_CONTENT = "err_content";
	//Map<String, Scriptable>objs=new HashMap<String, Scriptable>();
	private final DocumentSet documentSet;
	Map<String, DocumentScriptable> objs=new HashMap<String, DocumentScriptable>();
	public DocumentLoader(DocumentSet documentSet) {
		super();
		this.documentSet = documentSet;
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
		final DocumentRecord src=getDocumentSet().byId(id);

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
		objs.put(id, o);
		loadFromContent(src.content, o);
		return o;

	}
	public DocumentScriptable reload(String id) {
		DocumentScriptable res=objs.get(id);
		if (res==null) return byId(id);
		res.setContentAndSave(res.getDocument().content);
		return res;
	}
	private DocumentScriptable defaultDocumentScriptable(final DocumentRecord src) {
		return new DocumentScriptable(this, src);
	}
	public void loadFromContent(String newContent, DocumentScriptable dst) {
		//BlankScriptableObject tools=new BlankScriptableObject(jsSession().root);
		dst.clear();
		//tools.put("$", this);
		//tools.put("_", dst);
		//BlankScriptableObject scope = new BlankScriptableObject(jsSession().root);
		//scope.setPrototype(tools);
		DocumentLoaderScriptable loaderScope = new DocumentLoaderScriptable(jsSession().root, this, dst);
		try {
			jsSession().eval("Load(id="+dst.getDocument().id+")", newContent, loaderScope);
			dst.put(HttpContext.ATTR_SCOPE, loaderScope.scope());
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(this , dst.getDocument().id+" has invalid content "+newContent);
			dst.put(ERROR_CONTENT, newContent );
		}
	}
	private JSSession jsSession() {
		return JSSession.cur.get();
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
	public void searchByQuery(final Query q, final Function iter) {
		Log.d(this, "Search by "+q);
		getDocumentSet().all(new DocumentAction() {

			@Override
			public boolean run(DocumentRecord d) {
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
		SuperclassPrototype res = new SuperclassPrototype(superclass);
		Scriptables.extend(res, overrideMethods);
		return res;
	}
	Authenticator auth;
	public Authenticator authenticator() {
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
	public void importDocuments(Set<DocumentRecord> drs) throws SqlJetException {
		Set<DocumentScriptable> willReload=new HashSet<DocumentScriptable>();
		for (DocumentRecord dr:drs) {
			DocumentRecord existentDr=documentSet.byId(dr.id);
			if (existentDr!=null) {
				copyDocumentExceptDates(dr , existentDr);
				documentSet.save(existentDr);
				//Log.d("Import", dr.content);
				//ds.setContentAndSave(dr.content);
				/*loadFromContent(dr.content, ds);
			ds.refreshSummary();*/
			} else {
				DocumentRecord newDr=getDocumentSet().newDocument(dr.id);
				copyDocumentExceptDates(dr, newDr);
				documentSet.save(newDr);
				/*final DocumentScriptable res=defaultDocumentScriptable(ndr);
			objs.put(dr.id, res);
			Log.d("Import", ndr.content);
			res.setContentAndSave(ndr.content);*/
				//loadFromContent(ndr.content, res);
			}
			if (objs.containsKey(dr.id)) willReload.add(objs.get(dr.id));
		}
		for (DocumentScriptable ds:willReload) {
			ds.reloadFromContent();
			//loadFromContent(ds.getDocument().content, ds);
		}
	}
}
