package jp.tonyu.js;


import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import jp.tonyu.debug.Log;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class Scriptables {
	public static final String CONSTRUCTOR = "constructor";
	public static final String PROTOTYPE = "prototype";

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
	public static void each(Scriptable s, AllPropAction action) {
		SortedSet<Integer> ord=new TreeSet<Integer>();
		for (Object o:s.getIds()) {
			if (o instanceof Number) {
				Number key = (Number) o;
				ord.add(key.intValue());
			} else {
				action.run(o, s.get((String)o, s));
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
				//Log.d("Scriptables", "Put "+k+"="+value);
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

	public static void extend(Scriptable to, Scriptable from) {
		for (Object k:from.getIds()) {
			if (k instanceof String) {
				String kstr = (String) k;
				Object value=ScriptableObject.getProperty(from, kstr); 
				ScriptableObject.putProperty(to, kstr, value);
			}
			if (k instanceof Number) {
				int kint = ((Number) k).intValue();
				Object value=ScriptableObject.getProperty(from, kint); 
				ScriptableObject.putProperty(to, kint, value);
			}
		}
		
	}
	public static Scriptable getAsScriptable(Scriptable obj,String name) {
		Object r=ScriptableObject.getProperty(obj, name);
		if (r instanceof Scriptable) {
			Scriptable s = (Scriptable) r;
			return s;
		}
		return null;
	}

	public static String getAsString(Scriptable obj, String name,Object defValue) {
		Object r=ScriptableObject.getProperty(obj, name);
		if (r instanceof String) {
			String s = (String) r;
			return s;
		}
		if (defValue instanceof String) {
			String sd = (String) defValue;
			return sd;
		}
		if (defValue!=null) {
			if (r==null) return null;
			return r+"";
		}
		return null;
	}
	public static Function getSuperclass(Scriptable obj) {
		if (obj==null) return null;
		Object res = ScriptableObject.getProperty(obj, PROTOTYPE);
		if (res instanceof Scriptable) {
			Scriptable s = (Scriptable) res;
			return getClass(s);
		}
		return null;
	}

	private static Function getClass(Scriptable obj) {
		Object res = ScriptableObject.getProperty(obj, CONSTRUCTOR);
		return null;
	}

}
