package jp.tonyu.soytext2.js;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.tmatesoft.sqljet.core.SqlJetException;

import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.db.SDB;
import jp.tonyu.soytext2.document.Document;

public class DocumentLoader {
	//private static final Object LOADING = "LOADING";
	public static final Pattern idpatWiki=Pattern.compile("\\[\\[([^\\]]+)\\]\\]");
		//Map<String, Scriptable>objs=new HashMap<String, Scriptable>();
	SDB db;
	Map<String, DocumentScriptable> objs=new HashMap<String, DocumentScriptable>();
	public DocumentScriptable byId(String id) {
		Document src=db.byId(id);
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
	public void newDocument(Scriptable hash) {
		
	}
	public void extend(DocumentScriptable dst, Scriptable hash) {
		for (Object key:hash.getIds()) {
			if (key instanceof String) {
				String str = (String) key;
				Matcher m=idpatWiki.matcher(str);
				Object value = hash.get(str, null);
				if (m.matches()) {
					String id=m.group(1);
					dst.put(db.byId(id), value);
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
