package jp.tonyu.util;

import java.util.HashMap;
import java.util.Map;

public class Maps {
	public static <K,V> MapIterator<K,V> entries(final Map<K,V> m) {
		return new MapIterator<K,V>(m);
	}
	public static void main(String[] args) {
		Map<String, String> str=new HashMap<String, String>();
		str.put("aho", "baka");
		str.put("uho", "bgaka");
		Maps.entries(str).each(new MapAction<String, String>() {
			
			@Override
			public void run(String key, String value) {
				System.out.println(key+"="+value);
			}
		});
	}
	public static <K,V> MapBuilder<K,V> create(K key, V value) {
		return new MapBuilder<K,V>(key,value);
	}
}

