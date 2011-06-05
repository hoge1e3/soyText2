package jp.tonyu.soytext2.js;

import jp.tonyu.js.BuiltinFunc;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

public abstract class CompileResultScriptable implements CompileResult, Function {

	public CompileResultScriptable(Scriptable scriptable) {
		this.scriptable=scriptable;
		scriptable.put(CompileResult.getDocumentSourceName, scriptable, new BuiltinFunc() {
			
			@Override
			public Object call(Context cx, Scriptable scope, Scriptable thisObj,
					Object[] args) {
				return CompileResultScriptable.this.getDocumentSource();
			}
		});
		scriptable.put(CompileResult.isUp2DateName, scriptable, new BuiltinFunc() {
			
			@Override
			public Object call(Context cx, Scriptable scope, Scriptable thisObj,
					Object[] args) {
				return CompileResultScriptable.this.isUp2Date();
			}
		});
	}
	final Scriptable scriptable;

	public void delete(int arg0) {
		scriptable.delete(arg0);
	}

	public void delete(String arg0) {
		scriptable.delete(arg0);
	}

	public Object get(int arg0, Scriptable arg1) {
		return scriptable.get(arg0, arg1);
	}

	public Object get(String arg0, Scriptable arg1) {
		return scriptable.get(arg0, arg1);
	}

	public String getClassName() {
		return scriptable.getClassName();
	}

	public Object getDefaultValue(Class<?> arg0) {
		return scriptable.getDefaultValue(arg0);
	}

	public Object[] getIds() {
		return scriptable.getIds();
	}

	public Scriptable getParentScope() {
		return scriptable.getParentScope();
	}

	public Scriptable getPrototype() {
		return scriptable.getPrototype();
	}

	public boolean has(int arg0, Scriptable arg1) {
		return scriptable.has(arg0, arg1);
	}

	public boolean has(String arg0, Scriptable arg1) {
		return scriptable.has(arg0, arg1);
	}

	public boolean hasInstance(Scriptable arg0) {
		return scriptable.hasInstance(arg0);
	}

	public void put(int arg0, Scriptable arg1, Object arg2) {
		scriptable.put(arg0, arg1, arg2);
	}

	public void put(String arg0, Scriptable arg1, Object arg2) {
		scriptable.put(arg0, arg1, arg2);
	}

	public void setParentScope(Scriptable arg0) {
		scriptable.setParentScope(arg0);
	}

	public void setPrototype(Scriptable arg0) {
		scriptable.setPrototype(arg0);
	}
	@Override
	public Object call(Context arg0, Scriptable arg1, Scriptable arg2,
			Object[] arg3) {
		return ((Function)scriptable).call(arg0, arg1, arg2, arg3);
	}
	@Override
	public Scriptable construct(Context arg0, Scriptable arg1, Object[] arg2) {
		// TODO Auto-generated method stub
		return ((Function)scriptable).construct(arg0, arg1, arg2);
	}
}
