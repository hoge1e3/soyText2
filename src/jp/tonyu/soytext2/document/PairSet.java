package jp.tonyu.soytext2.document;

import java.util.HashSet;
import java.util.Iterator;

public class PairSet<K,V> implements Iterable<Pair<K,V>>{
	HashSet<Pair<K, V>> h=new HashSet<Pair<K,V>>();
	public void put(final K key,final V value) {
		h.add(Pair.create(key,value));
	}
	@Override
	public Iterator<Pair<K, V>> iterator() {
		return h.iterator();
	}
	@Override
	public String toString() {
		StringBuilder b=new StringBuilder();
		for (Pair<K,V> p:h) {
			b.append(p+", ");
		}
		return "{"+b+"}";
	}
}
