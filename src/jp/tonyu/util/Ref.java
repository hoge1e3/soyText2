package jp.tonyu.util;

public class Ref<T> {
	T val;
	public T get() {return val;}
	public T set(T v) {return val=v;}
	public boolean notNull() {
		return val!=null;
	}
}
