package jp.tonyu.soytext2.js;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.db.SDB;
import jp.tonyu.soytext2.document.Document;
import jp.tonyu.soytext2.document.DocumentAction;
import jp.tonyu.soytext2.document.DocumentSet;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

public class DocumentLoader implements Wrappable {
	//private static final Object LOADING = "LOADING";
	public static final Pattern idpatWiki=Pattern.compile("\\[\\[([^\\]]+)\\]\\]");
		//Map<String, Scriptable>objs=new HashMap<String, Scriptable>();
	private final DocumentSet documentSet;
	Map<String, DocumentScriptable> objs=new HashMap<String, DocumentScriptable>();
	public DocumentLoader(DocumentSet documentSet) {
		super();
		this.documentSet = documentSet;
	}
	public DocumentScriptable byId(String id) {
		Document src=getDocumentSet().byId(id);
		if (src==null) return null;
		DocumentScriptable o=objs.get(id);
		if (o!=null) return o;
		o=new DocumentScriptable(src);
		objs.put(id, o);
		Map<String, Object> vars=new HashMap<String, Object>();
		vars.put("$", this);
		vars.put("_", o);
		try {
			RunScript.eval(src.content, vars);
		} catch (Exception e) {
			e.printStackTrace();
			Log.die(src.id+" has invalid content "+src.content);
		}
		return o;
	}
	public DocumentScriptable newDocument(Scriptable hash) {
		Object id = hash!=null ? hash.get("id", hash) : null;
		Document d;
		if (id instanceof String) {
			d=getDocumentSet().newDocument((String)id);
		} else {
			d=getDocumentSet().newDocument();
		}
		DocumentScriptable res=new DocumentScriptable(d);
		extend(res,hash);
		return res;
	}
	public void search(String cond, Scriptable tmpl, final Function iter) {
		getDocumentSet().all(new DocumentAction() {
			
			@Override
			public boolean run(Document d) {
				DocumentScriptable s=(DocumentScriptable) byId(d.id);
				Object brk=RunScript.call(iter, new Object[]{s});
				if (brk instanceof Boolean) {
					Boolean b = (Boolean) brk;
					if (b.booleanValue()) return true;
				}
				return false;
			}
		});
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
					dst.put(getDocumentSet().byId(id), value);
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
				return RunScript.call(func, new Object[]{src});
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
