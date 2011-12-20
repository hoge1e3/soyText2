package jp.tonyu.soytext2.ruby;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.mozilla.javascript.Scriptable;

import jp.tonyu.js.Scriptables;
import jp.tonyu.js.StringPropAction;
import jp.tonyu.js.Wrappable;

public class RubyEngine implements Wrappable {
	final Bindings b;
	final ScriptEngine engine;
    public RubyEngine(	ScriptEngine engine) {
    	b=engine.createBindings();
    	this.engine=engine;
	}
	public Object eval(String rubySrc) throws ScriptException {
		return eval(rubySrc,null);
	}
	public Object eval(String rubySrc,Scriptable scope) throws ScriptException {
        if (scope!=null) Scriptables.each(scope, new StringPropAction() {
			
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
