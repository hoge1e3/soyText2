package jp.tonyu.soytext2.ruby;

import javax.script.ScriptEngine;

import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.js.JSSession;

import org.jruby.RubyArray;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class ScriptableRubyInvocable {
	public static Object create(ScriptEngine engine,final Scriptable s) {
		return RubyInvocable.create(engine, new MethodMissingHandler() {
			
			@Override
			public Object methodMissing(String name, RubyArray args) {
				if (name.endsWith("=")) {
					String name2=name.substring(0,name.length()-1);
					ScriptableObject.putProperty(s,name2, args.get(0));
					return args.get(0);
				} else {
					Object r=ScriptableObject.getProperty(s, name);
					if (args.size()>0) {
						if (r instanceof Function) {
							Function f = (Function) r;
							return JSSession.cur.get().call(f, args.toArray());
						} else {
							Log.die("Cannot call "+r);
						}
					} else {
						return r;
					}
				}
				return null;
			}
		});
	}
}
