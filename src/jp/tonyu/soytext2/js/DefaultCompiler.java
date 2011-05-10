package jp.tonyu.soytext2.js;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import jp.tonyu.debug.Log;
import jp.tonyu.js.Convert;
import jp.tonyu.js.StringPropAction;
import jp.tonyu.parser.Parser;
import jp.tonyu.soytext.js.HttpContextFunctions;
import jp.tonyu.soytext2.servlet.HttpContext;
import jp.tonyu.soytext2.servlet.SWebApplication;
import jp.tonyu.util.MapAction;
import jp.tonyu.util.Maps;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

public class DefaultCompiler implements DocumentCompiler {
	public static final String ARGUMENTORDER = "argumentOrder";
	class ContextInfo {
		private static final String SCOPE = "scope";
		final public Map<String, Object> consts=new Hashtable<String, Object>();
		final public List<String> paramValues=new Vector<String>();
		final public Map<String, DocumentScriptable> compiledScope=new Hashtable<String, DocumentScriptable>();
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
							if (value instanceof DocumentScriptable) {
								DocumentScriptable sv = (DocumentScriptable) value;
								JSSession jsSession = JSSession.cur.get();
								jsSession.install(sv);								
								compiledScope.put(key, sv);
							}
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
	public CompileResult compile(DocumentScriptable s) {
		return defaultHtmlDocument(s);
	}
	static  final Pattern htmlPlain=Pattern.compile("([^<]*<[^%])*[^<]*");
	static  final Pattern embedLang=Pattern.compile("<%(=?) *(([^%]*%[^>])*[^%]*)%>");
	static  final String CONTEXT="context";
	static  final String PRINT="p",SAVE="save",SEARCH="search";

	public CompileResult defaultHtmlDocument(final DocumentScriptable d)  {
		Log.d(this,"Exec as Html");
		final ContextInfo inf=new ContextInfo(d);

		String src=""+d.get(HttpContext.bodyAttr);
		Parser p=new Parser(src);
		p.setSpacePattern(null);
		final StringBuilder buf=new StringBuilder();
		final JSSession jsSession = JSSession.cur.get();
		buf.append("res=function ("+Parser.join(", ",inf.paramValues)+") { \n"); {
			Maps.entries(inf.compiledScope).each(new MapAction<String, DocumentScriptable>() {
				@Override
				public void run(String key, DocumentScriptable value) {
					buf.append(key+"=this['"+jsSession.idref(value)+"'];\n");
				}
			});
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
		final Object res=jsSession.eval(buf.toString(), inf.consts);
		final long l=d.getDocument().lastUpdate;
		return new CompileResult() {

			@Override
			public DocumentScriptable src() {
				return d;
			}
			@Override
			public <T> T value(Class<T> type) {
				if (type==null || type.isAssignableFrom(res.getClass())) {
					return (T)res;
				}
				if (res instanceof Function && 	type.isAssignableFrom(SWebApplication.class)) {
					final Function f = (Function) res;
					SWebApplication app = new SWebApplication() {
						
						@Override
						public void run(Map<String, String> params) {
							Object[] args= new Object[inf.paramValues.size()];
							for (int i=0 ; i<args.length ; i++) {
								args[i]=params.get(inf.paramValues.get(i));
							}
							jsSession.call(f, args);
						}
					};
					return (T)app;
				}
				return null;
			}
			@Override
			public boolean isUp2Date() {
				return d.getDocument().lastUpdate==l;
			}

			@Override
			public DocumentCompiler compiler() {
				return DefaultCompiler.this;
			}
		};
	}

}
