package jp.tonyu.soytext2.js;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.value.Value;
import jp.tonyu.soytext2.value.Values;

public class ValueConvert {
	public static Value toValue(final Scriptable s) {
		final Function f;
		if (s instanceof Function) {
			f = (Function) s;			
		} else f=null;
		return new Value() {
			
			@Override
			public void put(Object key, Object value, Value options) {
				s.put(key+"", s, value);
			}
			
			@Override
			public Object get(Object key, Value options) {
				return s.get(key+"",s);
			}
			
			@Override
			public Object exec(Value context) {
				if (f==null) Log.die("Not supported "); 
				return JSSession.cur.get().call(f, Values.toArrayArgs(context));
			}
		};
	}
}
