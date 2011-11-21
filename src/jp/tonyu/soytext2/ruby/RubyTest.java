package jp.tonyu.soytext2.ruby;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.jruby.RubyArgsFile;
import org.jruby.RubyArray;

public class RubyTest {
	public static void main(String[] args) throws ScriptException {
		new RubyTest().main();
	}
	/*public Object method_missing(ScriptEngine engine,MethodMissingHandler h) throws ScriptException {
		Bindings b=engine.createBindings();
		b.put("$t", h);
		return engine.eval("class Test\n" +
				"   def method_missing(name,*args)" +
				"      $t.methodMissing(name,args)\n"+
				"   end\n" +
				"end\n" +
				"Test.new", b);
	}
	public void go(Object name,RubyArray args) {
		System.out.println("Call");
		System.out.println(name);
		System.out.println(args.toArray());
	}*/
	public void main() throws ScriptException {
		System.setProperty("org.jruby.embed.localvariable.behavior", "transient");
		ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jruby");
        Bindings b=engine.createBindings();
        b.put("window", this);
        b.put("a", RubyInvocable.create(engine, new MethodMissingHandler() {
			
			@Override
			public Object methodMissing(String name, RubyArray args) {
				Object a=args.get(0);
				Object b=args.get(1);
				return name+":"+b+a;
			}
		}));
        /*Object res=engine.eval("class Test\n"+
        "   def test(x)\n" +
        "      p x+2\n" +
        "      x*2\n" +
        "   end\n" +
        "end\n" +
        "Test.new");//.test(window.test(3))\n",b);*/
        Object res2=engine.eval("a.hoge(\"fuga\",\"haga\")",b);
        System.out.println(res2);
	}
	public int test(int x) {
		return x*20;
	}
}
