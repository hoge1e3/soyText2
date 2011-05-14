package jp.tonyu.js;


import org.mozilla.javascript.Scriptable;

public class Convert {
	public static Object[] toArray(Object scriptable) {
		if (scriptable instanceof Scriptable) {
			Scriptable ary = (Scriptable) scriptable;
			Integer len=toInt(ary.get("length", ary));
			if (len==null) len=0;
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

	public static Integer toInt(Object i) {
		if (i instanceof Integer) {
			return (Integer) i;
		}
		return null;
	}
	
	public static void each(Scriptable s, StringPropAction action) {
		for (Object o:s.getIds()) {
			if (o instanceof String) {
				String key = (String) o;
				action.run(key, s.get(key, s));
			}
		}
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
