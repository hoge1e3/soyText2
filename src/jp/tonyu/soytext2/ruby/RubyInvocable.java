package jp.tonyu.soytext2.ruby;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class RubyInvocable {
	public static Object create(ScriptEngine engine, MethodMissingHandler h)  {
		Bindings b=		engine.getBindings(ScriptContext.ENGINE_SCOPE);//engine.createBindings();
		b.put("$t", h);
		
		try {
			//System.out.println(b.get("RubyInvocable"));			
			Object res=engine.eval(
					"if not defined?(RubyInvocable) then \n" +
					"class RubyInvocable\n" +
					"    def initialize(t)\n" +
					"      @t=t\n" +
					"    end\n"+
					"    def method_missing(name,*args)" +
					"      @t.methodMissing(name,args)\n"+
					"    end\n" +
					"end\n" +
					"end\n" +
					"RubyInvocable.new($t)", b);
			//System.out.println(b.get("RubyInvocable"));
			return res;
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		return null;
	}
} 
