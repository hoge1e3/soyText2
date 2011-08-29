package jp.tonyu.util;

import jp.tonyu.debug.Log;

public class Ref<T> {
	T val;
	boolean isset=false;
	public Ref(T init) {	
		val=init;
		isset=true;
	}
	public Ref() {
	}
	public T get() {
		if (!isset) Log.die(this+" Not set"); 
		return val;
	}
	public boolean isSet() {
		return isset;
	}
	public T set(T v) {isset=true;return val=v;}
	public boolean notNull() {
		return val!=null;
	}
	public static <T> Ref<T> create(T val) {
		return new Ref<T>(val);
	}
}
