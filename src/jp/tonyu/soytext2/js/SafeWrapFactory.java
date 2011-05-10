package jp.tonyu.soytext2.js;

import jp.tonyu.debug.Log;
import jp.tonyu.js.Wrappable;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;


public class SafeWrapFactory extends WrapFactory {
	@Override
	public Scriptable wrapAsJavaObject(Context cx, Scriptable scope,
			Object javaObject, Class<?> staticType) {
		if (javaObject instanceof String ||
				javaObject instanceof Number ||
				javaObject instanceof Boolean ||
				javaObject instanceof org.mozilla.javascript.EvaluatorException ||
				javaObject instanceof org.mozilla.javascript.EcmaError ||
					javaObject instanceof Wrappable ) {
			return super.wrapAsJavaObject(cx, scope, javaObject, staticType);
		}
		Log.die(javaObject.getClass()+": Only Wrappable can be wrapped.");
		return null;
	}
}
