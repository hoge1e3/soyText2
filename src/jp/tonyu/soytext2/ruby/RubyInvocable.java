package jp.tonyu.soytext2.ruby;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class RubyInvocable {
	public static Object create(ScriptEngine engine, MethodMissingHandler h)  {
		Bindings b=engine.createBindings();
		b.put("$t", h);
		try {
			return engine.eval("class Test\n" +
					"   def method_missing(name,*args)" +
					"      $t.methodMissing(name,args)\n"+
					"   end\n" +
					"end\n" +
					"Test.new", b);
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		return null;
	}
} 
