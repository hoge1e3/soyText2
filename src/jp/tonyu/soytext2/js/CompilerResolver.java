package jp.tonyu.soytext2.js;


public class CompilerResolver {
	static DocumentCompiler def=new DefaultCompiler();
	private static DocumentCompiler resolve(DocumentScriptable d) {
		return def;
	}
	static CompileResult compile(DocumentScriptable d) {
		DocumentCompiler c = resolve(d);
		if (c==null) return null;
		return c.compile(d);
	}

}
