package jp.tonyu.js;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

public abstract class BuiltinFunc implements Function {
	/*public abstract Object onCall(Object[] args);
	@Override
	public Object call(Context cx, Scriptable scope, Scriptable thisObj,
			Object[] args) {
		return onCall(args);
	}*/
	@Override
	public abstract Object call(Context cx, Scriptable scope, Scriptable thisObj,Object[] args);
			
	@Override
	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(int index) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object get(String name, Scriptable start) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean has(int index, Scriptable start) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasInstance(Scriptable instance) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void put(String name, Scriptable start, Object value) {
		// TODO Auto-generated method stub
		
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
