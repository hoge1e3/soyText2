package jp.tonyu.js;


import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class BlankFunction extends BlankScriptableObject implements Function {

	public BlankFunction() {
		super();
	}

	public BlankFunction(Scriptable scope) {
		super(scope);
	}

	@Override
	public Object call(Context cx, Scriptable scope, Scriptable thisObj,
			Object[] args) {
		return null;
	}

	@Override
	public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		return null;
	}
}
