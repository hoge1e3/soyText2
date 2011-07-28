package jp.tonyu.js;


import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import jp.tonyu.debug.Log;

import org.mozilla.javascript.Scriptable;

public class Scriptables {
	public static Object[] toArray(Object scriptable) {
		if (scriptable instanceof Scriptable) {
			Scriptable ary = (Scriptable) scriptable;
			Integer len=toInt(ary.get("length", ary),0);
			Object[] res=new Object[len];
			for (int i=0 ; i<len ; i++) {
				res[i]=ary.get(i, ary);
			}
			return res;
		}
		if (scriptable instanceof Object[]) {
			Object[] res = (Object[]) scriptable;
			return res;
		}
		return new Object[0];
	}

	public static Integer toInt(Object i,Integer defValue) {
		if (i instanceof Number) {
			return ((Number) i).intValue();
		}
		return defValue;
	}
	
	public static void each(Scriptable s, StringPropAction action) {
		for (Object o:s.getIds()) {
			if (o instanceof String) {
				String key = (String) o;
				action.run(key, s.get(key, s));
			}
		}
	}
	public static void each(Scriptable s, NumberPropAction action) {
		SortedSet<Integer> ord=new TreeSet<Integer>();
		for (Object o:s.getIds()) {
			if (o instanceof Number) {
				Number key = (Number) o;
				ord.add(key.intValue());
			}
		}
		for (int i: ord) {
			action.run(i, s.get(i, s));
		}
	}

	public static Map<String,Object> toStringKeyMap(Scriptable s) {
		Map<String, Object> res = new HashMap<String, Object>();
		extend(res,s);
		return res;
	}
	public static void extend(Map<String,Object> map,Scriptable s) {
		for (Object k:s.getIds()) {
			if (k instanceof String) {
				String sk = (String) k;
				Object value = s.get(sk, s);
				map.put(sk, value);				
			}
		}
	}
	public static Scriptable extend(Scriptable s,Map<?,?> map) {
		for (Object k:map.keySet()) {
			Object value=map.get(k);
			if (k instanceof String) {
				String kstr = (String) k;
				Log.d("Scriptables", "Put "+k+"="+value);
				s.put(kstr, s, value);
			}
			if (k instanceof Number) {
				Number n = (Number) k;
				s.put(n.intValue(), s, value);
			}
		}
		return s;
		
	}
    public static String literal(String raw) {
   	 String cook=raw;
   	 cook=cook.replaceAll("\\\\", "\\\\\\\\");
   	 cook=cook.replaceAll("\\n","\\\\n");
   	 cook=cook.replaceAll("\\r","\\\\r");
   	 cook=cook.replaceAll("'","\\\\'");
   	 return "'"+cook+"'";
    }

}
