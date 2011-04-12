package jp.tonyu.soytext2.js;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.tonyu.soytext2.db.SDB;
import jp.tonyu.soytext2.document.Document;
import jp.tonyu.soytext2.document.DocumentSet;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

public class DocumentLoader {
	//private static final Object LOADING = "LOADING";
	public static final Pattern idpatWiki=Pattern.compile("\\[\\[([^\\]]+)\\]\\]");
		//Map<String, Scriptable>objs=new HashMap<String, Scriptable>();
	final DocumentSet documentSet;
	Map<String, DocumentScriptable> objs=new HashMap<String, DocumentScriptable>();
	public DocumentLoader(DocumentSet documentSet) {
		super();
		this.documentSet = documentSet;
	}
	public DocumentScriptable byId(String id) {
		Document src=documentSet.byId(id);
		DocumentScriptable o=objs.get(id);
		if (o!=null) return o;
		o=new DocumentScriptable(src);
		objs.put(id, o);
		Map<String, Object> vars=new HashMap<String, Object>();
		vars.put("$", this);
		vars.put("_", o);
		RunScript.eval(src.content, vars);
		return o;
	}
	public DocumentScriptable newDocument(Scriptable hash) {
		Object id = hash.get("id", hash);
		Document d;
		if (id instanceof String) {
			d=documentSet.newDocument((String)id);
		} else {
			d=documentSet.newDocument();
		}
		DocumentScriptable res=new DocumentScriptable(d);
		extend(res,hash);
		return res;
	}
	public void extend(DocumentScriptable dst, Scriptable hash) {
		for (Object key:hash.getIds()) {
			if (key instanceof String) {
				String str = (String) key;
				Matcher m=idpatWiki.matcher(str);
				Object value = hash.get(str, null);
				if (m.matches()) {
					String id=m.group(1);
					dst.put(documentSet.byId(id), value);
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
}
