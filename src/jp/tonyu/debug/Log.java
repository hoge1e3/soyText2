package jp.tonyu.debug;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;



public class Log {
	static LogWindow lw;
	public static void showLogWindow(Runnable onClose) {
		if (lw==null) {
			lw=new LogWindow(onClose);
		}
		lw.setVisible(true);
	}
	static HashSet<String> whiteList=new HashSet<String>();
	static HashSet<String> blackList=new HashSet<String>();
	static boolean useWhiteList=false;
	static {
		String[] whiteLista=new String[] { "updateIndex"};
		String[] blackLista=new String[] {};
		for (String s:whiteLista) {
			whiteList.add(s);
		}
		for (String s:blackLista) {
			blackList.add(s);
		}
	}
	public static void d(Object tag,Object content) {
		//if ("ToValues".equals(tag) || "ClassIdx".equals(tag) ||"getSPClass".equals(tag)) {
		if ( tagMatch(tag) && wordMatch(content)) {
			String cont = "["+tag+"]"+content;
			System.out.println(cont);
			if (lw!=null) lw.println(cont);
		}
	}

	private static boolean wordMatch(Object content) {
		return true;//(content+"").indexOf("root@")>=0;
	}

	private static boolean tagMatch(Object tag) {
		return (useWhiteList && whiteList.contains(tag+"")) ||
				(!useWhiteList && !blackList.contains(tag+""));
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
