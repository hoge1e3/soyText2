package jp.tonyu.soytext2.js;

import jp.tonyu.debug.Log;

import org.mozilla.javascript.Scriptable;


public class CompilerResolver {
	static DocumentCompiler def=new DefaultCompiler(false);
	//static DocumentCompiler defSimple=new DefaultCompiler(true);
	//static DocumentCompiler defSimple=new DefaultCompiler();
	private static DocumentCompiler resolve(DocumentScriptable d) {
		/*String name = d.get("name")+"";
		if (name.endsWith(".js") && !"extJS".equals(d.get("compiler"))) {
			Log.d("CR", "Use simple compiler for "+name+" "+d);
			return defSimple;
		}*/
		return def;
	}
	static CompileResult compile(DocumentScriptable d) {
		DocumentCompiler c = resolve(d);
		if (c==null) return null;
		return c.compile(d);//injectSource(c.compile(d),d);
	}
	/*static private CompileResult injectSource(CompileResult c, DocumentScriptable src) {
		try {
			Scriptable r = c.value(Scriptable.class);
			if (r!=null) {
				Log.d("injectSource", "Injected: "+r+" -> "+src);
				r.put(DefaultCompiler.ATTR_SRC, r, src);
			}
		} catch (Exception e) {
			Log.d("injectSource", "not convert into scriptable"+c.value(Object.class));
		}
		return c;
	}*/

}
