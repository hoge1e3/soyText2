package jp.tonyu.soytext2.js;

import java.util.Map;

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
				javaObject instanceof org.mozilla.javascript.JavaScriptException ||
				//javaObject instanceof org.mozilla.javascript.JavaException ||
					javaObject instanceof Wrappable ) {
			return super.wrapAsJavaObject(cx, scope, javaObject, staticType);
		}
		if (javaObject instanceof Map) {
			return new MapScriptable((Map)javaObject);
		}
		Log.die(javaObject.getClass()+": Only Wrappable can be wrapped.");
		return null;
	}
}
