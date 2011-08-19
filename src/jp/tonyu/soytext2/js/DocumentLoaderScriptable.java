package jp.tonyu.soytext2.js;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jp.tonyu.debug.Log;
import jp.tonyu.js.BlankScriptableObject;
import jp.tonyu.js.Scriptables;
import jp.tonyu.js.Wrappable;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.UniqueTag;

public class DocumentLoaderScriptable implements Scriptable, Wrappable {
	public static final String LOADER="$",THISDOC="_",DB="db";
	public final IDocumentLoader loader;
	private Scriptable parentScope;
	public final DBHelper dbscr;
	public DocumentLoaderScriptable(Scriptable parentScope, IDocumentLoader loader,
			DocumentScriptable thisDoc) {
		super();
		this.parentScope=parentScope;
		this.loader = loader;
		this.thisDoc = thisDoc;
		if (loader instanceof DocumentLoader) {
			DocumentLoader ld = (DocumentLoader) loader;
			this.dbscr=new DBHelper(ld);			
		} else {
			this.dbscr=null;
		}
	}
	public final DocumentScriptable thisDoc;
	private final Set<String> undefinedSymbols=new HashSet<String>();
	@Override
	public void delete(String name) {
		// TODO Auto-generated method stub

	}
	public Set<String> getUndefinedSymbols() {
		return undefinedSymbols;
	}

	@Override
	public void delete(int index) {
		// TODO Auto-generated method stub

	}
	final Map<String,Object> values=new HashMap<String,Object>();
	@Override
	public Object get(String name, Scriptable start) {
		if (LOADER.equals(name)) return loader;
		if (THISDOC.equals(name)) return thisDoc;
		if (DB.equals(name)) return dbscr;
		Object res = values.get(name);
		if (res==null) {
			if (parentScope.has(name, parentScope)) return UniqueTag.NOT_FOUND;
			undefinedSymbols.add(name);
			return null;
		}
		return res;
	}
	public Scriptable scope() {
		BlankScriptableObject res = new BlankScriptableObject();
		Scriptables.extend(res, values);
		return res;
	}

	@Override
	public Object get(int index, Scriptable start) {
		// TODO Auto-generated method stub
		return null;
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
		Object[] res=new Object[values.size()];
		int i = 0;
		for (String k:values.keySet()) {
			res[i]=k;
			i++;
		}
		return res;
	}

	@Override
	public Scriptable getParentScope() {
		return parentScope;
	}

	@Override
	public Scriptable getPrototype() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean has(String name, Scriptable start) {
		return values.containsKey(name);
	}

	@Override
	public boolean has(int index, Scriptable start) {
		return false;
	}

	@Override
	public boolean hasInstance(Scriptable instance) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void put(String name, Scriptable start, Object value) {
		Log.d(this, "Put - "+name+"="+value);
		values.put(name, value);
		if (value instanceof Undefined) {
			undefinedSymbols.add(name);
		} else {
			undefinedSymbols.remove(name);
		}
	}

	@Override
	public void put(int index, Scriptable start, Object value) {
		// TODO Auto-generated method stub

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
