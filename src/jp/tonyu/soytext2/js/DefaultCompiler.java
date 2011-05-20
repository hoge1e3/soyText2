package jp.tonyu.soytext2.js;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.tonyu.debug.Log;
import jp.tonyu.js.Convert;
import jp.tonyu.js.StringPropAction;
import jp.tonyu.parser.Parser;
import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.extjs.ExtJSCompiler;
import jp.tonyu.soytext2.servlet.DocumentProcessor;
import jp.tonyu.soytext2.servlet.HttpContext;
import jp.tonyu.soytext2.servlet.SWebApplication;
import jp.tonyu.util.Literal;
import jp.tonyu.util.MapAction;
import jp.tonyu.util.Maps;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

public class DefaultCompiler implements DocumentCompiler {
	public static final String ARGUMENTORDER = "argumentOrder";
	class HeaderInfo {
		final public DocumentScriptable doc;
		
		final public Map<String, Object> consts=new Hashtable<String, Object>();
		final public List<String> paramValues=new Vector<String>();
		final public Map<String, DocumentScriptable> compiledScope=new Hashtable<String, DocumentScriptable>();
		final public Map<String, String> paramTypes=new Hashtable<String, String>();
		public HeaderInfo(final DocumentScriptable d) {
			doc=d;
			Object scope=d.get(ATTR_SCOPE);
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
							paramTypes.put(key, value+"");
						} else {
							if (value instanceof DocumentScriptable) {
								DocumentScriptable sv = (DocumentScriptable) value;
								/*JSSession jsSession = JSSession.cur.get();
								jsSession.install(sv);*/								
								compiledScope.put(key, sv);
							}
							/*Object putval = CompilerResolver.compile(d); // evalValue(d, v);  // evalUnlocked
							if (putval!=null) consts.put(key, putval);		*/
						}
					}
				});
			}
			consts.put(SRCSYM, d);
			HttpContextFunctions.load(consts);

		}
	}
	@Override
	public CompileResult compile(DocumentScriptable s) {
		String n=""+s.get("name");
		if (n.endsWith(".html")) {
			return defaultHtmlDocument(s);
		} else if (n.endsWith(".class.js")) {
			return new ExtJSCompiler().compile(s);
		} else if (n.endsWith(".js")) {
			return defaultJSDocument(s);
		}
		return null;
	}
	static  final Pattern htmlPlain=Pattern.compile("([^<]*<[^%])*[^<]*");
	static  final Pattern embedLang=Pattern.compile("<%(=?) *(([^%]*%[^>])*[^%]*)%>");
	static  final String CONTEXT="context";
	static  final String PRINT="p",SAVE="save",SEARCH="search";
	protected static final String ATTR_SRC = "src";
	protected static final String SRCSYM = "__src__";
	public static final String ATTR_SCOPE = "scope";

	public CompileResult defaultJSDocument(final DocumentScriptable d)  {
		Log.d(this,"Exec as JS");
		final HeaderInfo inf=new HeaderInfo(d);
		final StringBuilder buf=new StringBuilder();
		includeScope(inf, buf);
		buf.append(d.get(HttpContext.ATTR_BODY));
		return runeval(inf, buf);
	}
	public CompileResult defaultHtmlDocument(final DocumentScriptable d)  {
		Log.d(this,"Exec as Html");
		final HeaderInfo inf=new HeaderInfo(d);
		final String src=""+d.get(HttpContext.ATTR_BODY);
		final Parser p=new Parser(src);
		p.setSpacePattern(null);
		final StringBuilder buf=new StringBuilder();
		buf.append("res=function ("+Parser.join(", ",inf.paramValues)+") { \n"); {
			includeScope(inf, buf);
			while (true) {
				p.read(htmlPlain);
				buf.append(PRINT+"("+Convert.literal(p.group())+");\n");
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
		buf.append("res."+ATTR_SRC+"="+SRCSYM+";res;");
		return runeval(inf, buf);
	}

	private void includeScope(final HeaderInfo inf, final StringBuilder buf) {
		final JSSession jsSession = JSSession.cur.get();
		Maps.entries(inf.compiledScope).each(new MapAction<String, DocumentScriptable>() {
			@Override
			public void run(String key, DocumentScriptable value) {
				buf.append(key+"="+
						HttpContextFunctions.BYID+"("+Literal.toLiteral(value.getDocument().id)+
						").compile(Function);\n");
			}
		});
	}

	private CompileResult runeval(final HeaderInfo inf, final StringBuilder buf) {
		final DocumentScriptable d=inf.doc;
		final JSSession jsSession=JSSession.cur.get();
		final Object res=jsSession.eval(buf.toString(), inf.consts);
		final long l=d.getDocument().lastUpdate;
		Log.d(this ,"EvalBuf - "+buf);
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
						public void run() {
							final HttpContext httpContext = HttpContext.cur.get();
							final Map<String, String> hparams = httpContext.params();
							final String []hargs= httpContext.execArgs();
							//Map<String, Object> params=params(hparams, inf.paramTypes);
							final Object[] args= new Object[inf.paramValues.size()];
							for (int i=0 ; i<args.length ; i++) {
								final String key = inf.paramValues.get(i);
								final String typeHint=inf.paramTypes.get(key);
								String value=hparams.get(key);
								if (value==null && i<hargs.length) {
									value=hargs[i];
								}								
								args[i]=param(value, typeHint);
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
	private static DocumentLoader documentLoader() {
		return HttpContext.cur.get().documentLoader;
	}
	public static Object param(String value, Object _typeHint) {
		String typeHint=_typeHint+"";
		Object o;
		if (value==null) {
			o=null;
		} else	if (typeHint.startsWith("?doc")) {
			Matcher m = DocumentProcessor.idpatWiki.matcher(value);
			String id;
			if (m.lookingAt()) {
				id=m.group(1);
			} else id=value;
			o=documentLoader().byId(id);									
		} else 	if (typeHint.startsWith("?str")) {
			o=value;
		} else {
			Matcher m = DocumentProcessor.idpatWiki.matcher(value);
			String id;
			if (m.lookingAt()) {
				id=m.group(1);
				o=documentLoader().byId(id);
			} else {
				m = Literal.DQ.matcher(value);
				if (m.lookingAt()) {
					o=Literal.fromQuoteStrippedLiteral(m.group(1));
				} else {
					o=value;
				}
			}
		}
		Log.d("Param", " o="+o+" src="+value+" type="+typeHint);
		return o;
	}
	/*public static Map<String,Object> params(final Map<String,String> p,final Map<String, ?> typeHints) {
    	final Map<String,Object> res=new HashMap<String, Object>();
    	final DocumentLoader documentLoader=documentLoader();
    	Maps.entries(p).each(new MapAction<String, String>() {
			
			@Override
			public void run(String key, String value) {
				String typeHint = typeHints.get(key)+"";
				Object o;
				if (value==null) {
					o=null;
				} else	if (typeHint.startsWith("?doc")) {
					Matcher m = DocumentProcessor.idpatWiki.matcher(value);
					String id;
					if (m.lookingAt()) {
						id=m.group(1);
					} else id=value;
					o=documentLoader.byId(id);									
				} else 	if (typeHint.startsWith("?str")) {
					o=value;
				} else {
					Matcher m = DocumentProcessor.idpatWiki.matcher(value);
					String id;
					if (m.lookingAt()) {
						id=m.group(1);
						o=documentLoader.byId(id);
					} else {
						m = Literal.DQ.matcher(value);
						if (m.lookingAt()) {
							o=Literal.fromQuoteStrippedLiteral(m.group(1));
						} else {
							o=value;
						}
					}
				}
				Log.d("Param", key+"="+key+" o="+o+" src="+value+" type="+typeHint);
				res.put(key,o);
			}
		});
    	return res;
    }*/
}
