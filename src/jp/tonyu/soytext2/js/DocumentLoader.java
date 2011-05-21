package jp.tonyu.soytext2.js;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.tonyu.debug.Log;
import jp.tonyu.js.BlankScriptableObject;
import jp.tonyu.js.Wrappable;
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

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;


public class DocumentLoader implements Wrappable {
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
	public DocumentScriptable byId(String id) {
		final DocumentRecord src=getDocumentSet().byId(id);
		class Instanciator {
			public DocumentScriptable create() {
				return new DocumentScriptable(DocumentLoader.this, src);
			}
			public DocumentScriptable create(String prototypeID) {
				DocumentScriptable res = create();
				res.setPrototype(byId(prototypeID));
				return res;
			}		
		}
		if (src==null) return null;
		DocumentScriptable o=objs.get(id);
		if (o!=null) return o;
		Instanciator inst=new Instanciator();
		if (src.preContent==null) {
			o=inst.create();
		} else {
			try {
				o=(DocumentScriptable)jsSession().eval(src.preContent, Maps.create("$", (Object)inst));
			} catch(Exception e) {
				e.printStackTrace();
				Log.d(this, "Instanciation error - "+src.preContent);
				o=inst.create();
			}
		}
		objs.put(id, o);
		loadFromContent(src.content, o);
		return o;

	}
	public void loadFromContent(String newContent, DocumentScriptable dst) {
		Map<String, Object> vars=new HashMap<String, Object>();
		dst.clear();
		vars.put("$", this);
		vars.put("_", dst);
		try {
			jsSession().eval(newContent, vars);
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
		Object id = hash!=null ? hash.get("id", hash) : null;
		DocumentRecord d;
		if (id instanceof String) {
			d=getDocumentSet().newDocument((String)id);
		} else {
			d=getDocumentSet().newDocument();
		}
		DocumentScriptable res=new DocumentScriptable(this, d);
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
					Object brk=jsSession().call(iter, new Object[]{s});
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
		QueryBuilder qb=QueryBuilder.create(cond);
		if (tmpl!=null) {
			for (Object n:tmpl.getIds()) {
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
			}
		}
		final Query q=qb.toQuery();
		return q;
	}
	public void extend(DocumentScriptable dst, Scriptable hash) {
		if (hash==null) return;
		for (Object key:hash.getIds()) {
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
		}
	}
	public void setGetter(DocumentScriptable dst, final Function func) {
		dst.setGetter(new Getter() {

			@Override
			public Object getFrom(Object src) {
				return jsSession().call(func, new Object[]{src});
			}
			
		});
	}
	public Wrappable newInstance(String className) {
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
}
