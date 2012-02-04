package jp.tonyu.soytext2.document;

public class Pair<K, V> {
	public final K key;
	public final V value;
	public Pair(K k, V v) {
		super();
		this.key = k;
		this.value = v;
	}
	public static <K,V> Pair<K,V> create(K k,V v) {
		return new Pair<K,V>(k,v);
	}
	@Override
	public int hashCode() {
		return key.hashCode()+value.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Pair) {
			Pair t = (Pair) obj;
			return key.equals(t.key) && value.equals(t.value);
		}
		return false;
	}
}
