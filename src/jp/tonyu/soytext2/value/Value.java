package jp.tonyu.soytext2.value;

public interface Value {
	public Object get(Object key, Value options);
	public void put(Object key, Object value, Value options);
	public Object exec(Value context);
}
