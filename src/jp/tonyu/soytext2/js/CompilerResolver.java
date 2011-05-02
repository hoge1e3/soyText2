package jp.tonyu.soytext2.js;

import jp.tonyu.soytext.js.DefaultCompiler;

public class CompilerResolver {
	static DocumentCompiler def=new DefaultCompiler();
	public static DocumentCompiler resolve(DocumentScriptable d) {
		return def;
	}
	public static Object compile(DocumentScriptable d) {
		DocumentCompiler c = resolve(d);
		if (c==null) return d;
		return c.compile(d);
	}

}
