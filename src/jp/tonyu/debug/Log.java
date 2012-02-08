package jp.tonyu.debug;

import java.io.PrintWriter;
import java.io.StringWriter;



public class Log {
	static LogWindow lw;
	public static void showLogWindow(Runnable onClose) {
		if (lw==null) {
			lw=new LogWindow(onClose);
		}
		lw.setVisible(true);
	}
	public static void d(Object tag,Object content) {
		//if ((tag+"").equals("QQuery")) {
			String cont = "["+tag+"]"+content;
			System.out.println(cont);
			if (lw!=null) lw.println(cont);
		//}
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

	public static void die(Exception e) {
		die("Wrapped Exception :"+e);
	}
	public static StringWriter errorLog=new StringWriter();
	public static void e(Exception e) {
		PrintWriter p = new PrintWriter(errorLog);
		e.printStackTrace(p);
		p.close();
	}
}
