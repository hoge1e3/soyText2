package jp.tonyu.debug;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Vector;

import jp.tonyu.util.TDate;



public class Log {
	static LogWindow lw;
	public static void showLogWindow(Runnable onClose) {
		if (lw==null) {
			lw=new LogWindow(onClose);
		}
		lw.setVisible(true);
	}
	static StringBuilder buf=new StringBuilder();
	static HashSet<String> whiteList=new HashSet<String>();
	static HashSet<String> blackList=new HashSet<String>();
	static boolean useWhiteList=false;
	static {
		String[] whiteLista=new String[] {"query","query-prep","JDBC","jp.tonyu.soytext2.document.SDB"};
		String[] blackLista=new String[] {};
		for (String s:whiteLista) {
			whiteList.add(s);
		}
		for (String s:blackLista) {
			blackList.add(s);
		}
		runThread();
	}
	public static void runThread() {
	    Thread t= new Thread() {
	        public void run() {
	            while(true) {
	                String r;
	                synchronized (buf) {
                        while (buf.length()==0) {
                            try {
                                buf.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        r=buf.toString();
                        buf.delete(0, buf.length());
                    }
	                System.out.print(r);
	                if (lw!=null) lw.print(r);
	            }
	        }
	    };
	    t.setDaemon(true);
	    t.start();
	}
	public static void addBuf(String s) {
	    synchronized (buf) {
            buf.append(s+"\n");
            buf.notifyAll();
        }
	}
	public static void d(Object tag,Object content) {
		//if ("ToValues".equals(tag) || "ClassIdx".equals(tag) ||"getSPClass".equals(tag)) {
		tag=convTag(tag);
	    if ( tagMatch(tag) && wordMatch(content)) {
			String cont = "["+tag+"]"+content;
			addBuf(new TDate().toString("yy/MM/dd HH:mm:ss.SSS")+":"+cont);
			//System.out.flush();
		}
	}

	private static Object convTag(Object tag) {
	    if (tag==null) return "null";
	    if (tag instanceof String) {
            String s=(String) tag;
            return s;
        }
	    if (tag instanceof Class) {
            Class<?> cl=(Class<?>) tag;
            return cl.getName();
        }
        return tag.getClass().getName();
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
		die("Log:die Wrapped Exception :"+e);
	}
	public static StringWriter errorLog=new StringWriter();
	public static void e(Exception e) {
		PrintWriter p = new PrintWriter(errorLog);
		e.printStackTrace(p);
		p.close();
	}
}
