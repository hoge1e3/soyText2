package jp.tonyu.soytext2.ruby;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jp.tonyu.js.Scriptables;
import jp.tonyu.js.StringPropAction;
import jp.tonyu.js.Wrappable;

import org.mozilla.javascript.Scriptable;

public class RubyEvaluator implements Wrappable {
	static {
		System.setProperty("org.jruby.embed.localvariable.behavior", "transient");
		ScriptEngineManager manager = new ScriptEngineManager();
		engine = manager.getEngineByName("jruby");
	}
	static ScriptEngine engine;
	public Object eval(String rubySrc,Scriptable scope) throws ScriptException {
        final Bindings b=engine.createBindings();
        Scriptables.each(scope, new StringPropAction() {
			
			@Override
			public void run(String key, Object value) {
				if (value instanceof Scriptable) {
					Scriptable s = (Scriptable) value;
					b.put(key, ScriptableRubyInvocable.create(engine,s));
				} else {
					b.put(key, value);
				}
			}
		});
        return engine.eval(rubySrc,b);
	}
}
