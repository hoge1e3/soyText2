package jp.tonyu.soytext2.js;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jp.tonyu.soytext.js.BuiltinFunc;
import jp.tonyu.soytext2.document.Document;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class DocumentScriptable implements Scriptable {
	private static final Object GETTERKEY = "GETTERKEY";
	Map<Object, Object>binds=new HashMap<Object, Object>();
	Document d;
	public DocumentScriptable(final Document d) {
		put("id",d.id);
		put("lastUpdate",d.lastUpdate);
		put("save",new BuiltinFunc() {
			
			@Override
			public Object call(Context cx, Scriptable scope, Scriptable thisObj,
					Object[] args) {
				d.save();
				return d;
			}
		});
	}
	public Object get(Object key) {
		Object res = binds.get(key);
		if (res!=null) return res;
		if (key instanceof DocumentScriptable) {
			DocumentScriptable keyDoc = (DocumentScriptable) key;
			Getter g=keyDoc.getGetter();
			if (g!=null) return g.getFrom(this);
		}
		return null;
	}
	public Getter getGetter() {
		return (Getter)get(GETTERKEY);
	}
	public void setGetter(Getter g) {
		put(GETTERKEY, g);
	}
	public Object put(Object key,Object value) {
		binds.put(key, value);
		return value;
	}
	public Set<Object> keySet() {
		return binds.keySet();
	}

	@Override
	public void delete(String name) {
		binds.remove(name);		
	}

	@Override
	public void delete(int index) {
		binds.remove(index);
	}

	@Override
	public Object get(String name, Scriptable start) {
		return get(name);
	}

	@Override
	public Object get(int index, Scriptable start) {
		return get(index);
	}

	@Override
	public String getClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getDefaultValue(Class<?> hint) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getIds() {
		Set<Object> keys=binds.keySet();
		Object[] res=new Object[keys.size()];
		int i=0;
		for (Object key:keys) {
			res[i]=key;
		}
		return res;
	}

	@Override
	public Scriptable getParentScope() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Scriptable getPrototype() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean has(String name, Scriptable start) {
		return binds.containsKey(name);
	}

	@Override
	public boolean has(int index, Scriptable start) {
		return binds.containsKey(index);
	}

	@Override
	public boolean hasInstance(Scriptable instance) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void put(String name, Scriptable start, Object value) {
		put(name,value);
	}

	@Override
	public void put(int index, Scriptable start, Object value) {
		put(index,value);
	}

	@Override
	public void setParentScope(Scriptable parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPrototype(Scriptable prototype) {
		// TODO Auto-generated method stub
		
	}

}
