package jp.tonyu.debug;



public class Log {
	public static void d(Object tag,Object content) {
		System.out.println("["+tag+"]"+content);
	}

	public static Object die(String string) {
		throw new RuntimeException(string);
		
	}

	public static void w(Object tag, Object content) {
		d(tag,content);
	}

	public static <T> T notNull(T value,String msg) {
		if (value==null) die(msg);
		return value;
	}
}
