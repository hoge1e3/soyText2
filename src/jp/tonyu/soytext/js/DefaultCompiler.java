package jp.tonyu.soytext.js;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import jp.tonyu.debug.Log;
import jp.tonyu.soytext.parser.Parser;
import jp.tonyu.soytext2.js.CompilerResolver;
import jp.tonyu.soytext2.js.DocumentCompiler;
import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.js.JSSession;
import jp.tonyu.soytext2.js.RunScript;
import jp.tonyu.soytext2.servlet.HttpContext;

import org.mozilla.javascript.Scriptable;

public class DefaultCompiler implements DocumentCompiler {
	class ContextInfo {
		private static final String SCOPE = "scope";
		private static final String ARGUMENTORDER = "argumentOrder";
		final public Map<String, Object> consts=new Hashtable<String, Object>();
		final public List<String> paramValues=new Vector<String>();
		public ContextInfo(final DocumentScriptable d) {
			Object scope=d.get(SCOPE);
			Object ord=d.get(ARGUMENTORDER);
			final boolean hasOrder;
			if (ord instanceof Scriptable) {
				Scriptable args = (Scriptable) ord;
				for (Object a:Convert.toArray(args)) {
					paramValues.add(a.toString());
				}
				hasOrder=true;
			} else hasOrder=false;
			if (scope instanceof Scriptable) {
				Convert.each((Scriptable)scope, new StringPropAction() {

					@Override
					public void run(String key, Object value) {
						if (value instanceof String && value.toString().startsWith("?")) {
							if (!hasOrder) {
								paramValues.add(key);
							}
						} else {
							JSSession.cur.get().install(d);
							/*Object putval = CompilerResolver.compile(d); // evalValue(d, v);  // evalUnlocked
							if (putval!=null) consts.put(key, putval);		*/
						}
					}
				});
			}

			HttpContextFunctions.load(consts);

		}
	}
	@Override
	public Object compile(DocumentScriptable s) {
		return defaultHtmlDocument(s);
	}
	static  final Pattern htmlPlain=Pattern.compile("([^<]*<[^%])*[^<]*");
	static  final Pattern embedLang=Pattern.compile("<%(=?) *(([^%]*%[^>])*[^%]*)%>");
	static  final String CONTEXT="context";
	static  final String PRINT="p",SAVE="save",SEARCH="search";

	public Scriptable defaultHtmlDocument(DocumentScriptable d)  {
		Log.d(this,"Exec as Html");
		ContextInfo i=new ContextInfo(d);

		String src=""+d.get(HttpContext.bodyAttr);
		Parser p=new Parser(src);
		p.setSpacePattern(null);
		StringBuilder buf=new StringBuilder();
		buf.append("var p=ctx.print;\n");
		buf.append("res=function ("+Parser.join(", ",i.paramValues)+") { \n"); {
			while (true) {
				p.read(htmlPlain);
				buf.append(PRINT+"("+RunScript.literal(p.group())+");\n");
				if (p.read(embedLang)) {
					if (p.group(1).length()>0) {
						buf.append(PRINT+"("+p.group(2)+");\n");
					} else {
						buf.append(p.group(2)+";");
					}
				} else {
					break;
				}
			}
		}
		buf.append("};");
		System.out.println("EvalBuf - "+buf);
		Object res=JSSession.cur.get().eval(buf.toString(), i.consts);
		if (res instanceof Scriptable) {
			Scriptable s = (Scriptable) res;
			return s;
		}
		return null;
	}

}
