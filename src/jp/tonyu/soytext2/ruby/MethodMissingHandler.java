package jp.tonyu.soytext2.ruby;

import org.jruby.RubyArray;

public interface MethodMissingHandler {
	public Object methodMissing(String name, RubyArray args);
}
