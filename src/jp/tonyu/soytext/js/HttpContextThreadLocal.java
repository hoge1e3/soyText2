package jp.tonyu.soytext.js;

import jp.tonyu.soytext2.servlet.HttpContext;

public class HttpContextThreadLocal {
	private static ThreadLocal<HttpContext> ctx=new ThreadLocal<HttpContext>() {
		protected HttpContext initialValue() {
			return null;
		}
	};
	public static HttpContext get() {return ctx.get();}
	public static void set(HttpContext value) {
		ctx.set(value);
	}
	public static void remove() {
		ctx.remove();
	}
}
