package jp.tonyu.soytext2.js;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jp.tonyu.debug.Log;
import jp.tonyu.js.BuiltinFunc;
import jp.tonyu.soytext.Origin;
import jp.tonyu.soytext2.document.Document;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class DocumentScriptable implements Scriptable {
	private static final Object GETTERKEY = "[[110414_051952@"+Origin.uid+"]]";
	Map<Object, Object>binds=new HashMap<Object, Object>();
	final Document d;
	final DocumentLoader loader;
	public Document getDocument() {
		return d;
	}
	public DocumentScriptable(final DocumentLoader loader,final Document d) {
		this.loader=loader;
		this.d=d;
		/*put("id",this , d.id );
		put("lastUpdate",this, d.lastUpdate);
		put("save",this, );*/
	}
	BuiltinFunc saveFunc =new BuiltinFunc() {
		
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			save();
			return DocumentScriptable.this;
		}
	};
	BuiltinFunc compileFunc =new BuiltinFunc() {
		
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			CompileResult c = JSSession.cur.get().compile(DocumentScriptable.this);
			/*if (args.length>=1 && args[0]!=null) {
				Log.d(this, args[0]+" -  Class="+args[0].getClass());
			//org.mozilla.javascript.IdFunctionObject
			}*/
			if (c==null) return DocumentScriptable.this;
			return c.value(Scriptable.class);
		}
	};

	public Object get(Object key) {
		if ("id".equals(key)) return d.id;
		if ("lastUpdate".equals(key)) return d.lastUpdate;
		if ("save".equals(key)) return saveFunc;
		if ("compile".equals(key)) return compileFunc;
		if (key instanceof DocumentScriptable) {
			DocumentScriptable keyDoc = (DocumentScriptable) key;
			key=JSSession.idref(keyDoc, d.documentSet);
		}
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
		if (key instanceof DocumentScriptable) {
			DocumentScriptable s = (DocumentScriptable) key;			
			binds.put(JSSession.idref(s, d.documentSet),value);
		} else	if (key instanceof String || key instanceof Number) {
			binds.put(key, value);
		} else if (value==null){
			binds.remove(key);
		} else {
			Log.die("Cannot put "+key);
		}
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
			/*if (key instanceof DocumentScriptable) {
				DocumentScriptable s = (DocumentScriptable) key;
				
				res[i] = JSSession.idref(s, d.documentSet);
				Log.d(this, "Put res["+i+"]="+res[i]);
			} else*/ 	if (key instanceof String || key instanceof Number) {
				res[i]=key;
			} else {
				Log.die("Wrong key! "+key);
			}
			i++;
		}
		for (Object r:res) {
			Log.d(this ," getids - "+r);
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
	public void save() {
		d.content="$.extend(_,"+HashLiteralConv.toHashLiteral(this)+");";
		Log.d(this, "content changed to "+d.content);
		d.save();
		
	}
	public void replaceContent(String content) {
		d.content=content;
		d.save();
		loader.loadFromContent(d, this);		
	}
	@Override
	public String toString() {
		return "(Docscr "+d.id+")";
	}
	public void clear() {
		binds.clear();
	}
}
