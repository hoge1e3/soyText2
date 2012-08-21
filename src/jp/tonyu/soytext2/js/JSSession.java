package jp.tonyu.soytext2.js;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jp.tonyu.debug.Log;
import jp.tonyu.js.BuiltinFunc;
import jp.tonyu.js.ContextRunnable;
import jp.tonyu.js.Jslint;
import jp.tonyu.js.Prototype;

import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.document.DocumentSet;
import jp.tonyu.util.MapAction;
import jp.tonyu.util.Maps;
import jp.tonyu.util.Resource;

import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class JSSession {
	private static final String UTIL = "SoyText";
	//public static final jp.tonyu.util.Context<JSSession> cur=new jp.tonyu.util.Context<JSSession>();
	public final Scriptable root;
	private Scriptable initObject(Context cx) {
		ScriptableObject o=cx.initStandardObjects();
		/*
		String[] builtinsa=new String[] {"String","Boolean","Number","Function","Object","Array","RegExp","undefined","null","true","false"
				,"NaN","Infinity","Date","Math","parseInt","TypeError","InternalError","JavaException"};
		Set<String> builtins=new HashSet<String>();
		for (String s:builtinsa) {
			builtins.add(s);
		}
		for (Object i: o.getAllIds()) {
			if (!builtins.contains(i)) {
				o.delete((String)i);
			} else {
				Object object = o.get(i.toString(),null);
				if (object instanceof NativeJavaObject) {
					NativeJavaObject j = (NativeJavaObject) object;
					Log.w("Runscript","Native java Object:"+j);
				}
			}
		}*/
		cx.evaluateString(o, Resource.text(Prototype.class,	 ".js"), "<prototype>", 1, null);
		cx.evaluateString(o, Resource.text(Jslint.class,	 ".js"), "<jslint>", 1, null);
		cx.evaluateString(o, Resource.text(HashLiteralConv.class,	 ".js"), "<hashLiteral>", 1, null);
		ScriptableObject.putProperty(o, "session", o);
		return o;
	}
	//private static Map<String,JSSession> ses=new HashMap<String, JSSession>();
	/*public static JSSession get(String sessionId) {
		Log.d("jssget", sessionId);
		JSSession res = ses.get(sessionId);
		if (res==null) {
			res=new JSSession();
			ses.put(sessionId, res);
		}
		Log.d("jssgetres", System.identityHashCode(res.root) );
		Log.d("jssgetres2", ScriptableObject.getProperty(res.root,"count") );
		for (Object k:res.root.getIds()) {
			Log.d("jssgetres2",k+" - "+ScriptableObject.getProperty(res.root, k.toString()));
		}
		return res;
	}*/
	JSSession() {
		Context c=Context.enter();

		root=initObject(c);
		objFactory=(Function)ScriptableObject.getProperty(root, "Object");
		aryFactory=(Function)ScriptableObject.getProperty(root, "Array");
		utils=(Scriptable)ScriptableObject.getProperty(root, UTIL);
		ScriptableObject.putProperty(utils, "decompile", HashLiteralConv.decompile);
		ScriptableObject.putProperty(utils, "isJavaNative", HashLiteralConv.isJavaNative);
		ScriptableObject.putProperty(utils, "isDocument", HashLiteralConv.isDocument);
		ScriptableObject.putProperty(utils, "debug", new Debug());
		ScriptableObject.putProperty(utils, "safeEval", safeEval);
		ScriptableObject.putProperty(utils, "contentChecker", newContentChecker);


		Context.exit();
	}
	   static BuiltinFunc newContentChecker=new BuiltinFunc() {
	       @Override
	    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {

	        return new ContentChecker(args[0]+"");
	    }
	   };
	static BuiltinFunc safeEval=new BuiltinFunc() {

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			String source=null,sourceName="safeEval";
			Scriptable sscope=null;
			if (args.length>=1 && args[0]!=null) {
				source=args[0].toString();
			}
			if (args.length>=2 && args[1] instanceof Scriptable) {
				sscope=(Scriptable)args[1];
			}
			if (args.length>=3 && args[2]!=null) {
				sourceName=args[2].toString();
			}
			if (scope!=null && source!=null) {
				Object res=cx.evaluateString(sscope, source, sourceName, 1, null);
				return res;
			}
			return null;
		}
	};
	Function objFactory,aryFactory;
	Scriptable utils;

	public Scriptable newObject() {
		return (Scriptable)withContext(new ContextRunnable() {
			@Override
			public Object run(Context cx) {
				return objFactory.construct(cx, root, new Object[0]);
			}
		});
	}
	public Scriptable newArray() {
		return (Scriptable)withContext(new ContextRunnable() {
			@Override
			public Object run(Context cx) {
				return aryFactory.construct(cx, root, new Object[0]);
			}
		});
	}
	public Object eval (String name, String s) {
		return eval(name, s, new HashMap<String, Object>());
	}
	/**
	 * Warning! scope must have root as parent scope
	 * @param name
	 * @param s
	 * @param scope   scope must have root as parent scope
	 * @return
	 */
	public Object eval (final String name, final String s , final Scriptable scope) {
		return withContext(new ContextRunnable() {
			@Override
			public Object run(Context cx) {
				try {
					Object result = cx.evaluateString(scope , s, name , 1, null);
					return result;
				} catch (EvaluatorException e) {
					e.printStackTrace();
					Log.die("JS -error at ||"+e.lineSource()+"|| "+e.details());
					return null;
				}
			}
		});

	}
	@SuppressWarnings("serial")
	public Object eval (final String name, final String s , final Map<String,Object> scope) {
		return withContext(new ContextRunnable() {

			@Override
			public Object run(Context cx) {
				try {
					final Scriptable s2=new ScriptableObject(root, null) {
						@Override
						public String getClassName() {
							return "Scope";
						}
					};
					Maps.entries(scope).each(new MapAction<String, Object>() {

						@Override
						public void run(String key, Object value) {
							s2.put(key, s2, value);
						}
					});
					//cx.setWrapFactory(new SafeWrapFactory());
					Object result = cx.evaluateString(s2 , s, name , 1, null);
					return result;
				} catch (EvaluatorException e) {
					Log.die("JS -error at ||"+e.lineSource()+"|| "+e.details());
					return null;
				}
			}
		});
	}
	public Object call(final Function f, final Object[] args) {
		return call(f,root,args);
	}
	public Object call(final Function f, final Scriptable thisObject, final Object[] args) {
		return withContext(new ContextRunnable() {
			@Override
			public Object run(Context cx) {
				//cx.setWrapFactory(new SafeWrapFactory());
				Object result = f.call(cx, root, thisObject , args);
				return result;
			}
		});
	}
	public static Object withContext(ContextRunnable action) {
		Context cx=Context.getCurrentContext();
		if (cx!=null) {
			return action.run(cx);
		}
		cx=Context.enter();
		try {
			//cx.setOptimizationLevel(-1);
			cx.setClassShutter(new ClassShutter() {
				Map<String,Boolean> cache=new HashMap<String, Boolean>();
				@Override
				public boolean visibleToScripts(String fullClassName) {
					if (cache.containsKey(fullClassName)) {
						return cache.get(fullClassName);
					}
					try {
						boolean res=false;
						Class<?> c=Class.forName(fullClassName);
						res=(Object.class.equals(c)) ||
							(Wrappable.class.isAssignableFrom(c)) ||
						    (Exception.class.isAssignableFrom(c));
						Log.d("ClassShutter", fullClassName+" - "+res);
						cache.put(fullClassName, res);
						return res;
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					return false;
				}
			});
			cx.setWrapFactory(new SafeWrapFactory());
			return action.run(cx);
		} finally {
			Context.exit();
		}
	}

	/*Map<String, CompileResult> compileCache=new HashMap<String, CompileResult>();
	public CompileResult compile(DocumentScriptable d) {
		String id = d.getDocument().id;
		CompileResult compileResult = compileCache.get(id);
		if (compileResult!=null) {
			if (compileResult.isUp2Date()) return compileResult;
		}
		CompileResult res=CompilerResolver.compile(d);
		compileCache.put(id, res);
		return res;
	}*/
	/*public void install(DocumentScriptable d) {
		CompileResult res=compile(d);
		root.put(idref(d, null), root, res.value(Scriptable.class));
	}*/
	public static String idref(DocumentScriptable d, DocumentSet viewPoint) {
		// viewPoint==null -> full path
		return "[["+d.getDocument().id+"]]";
	}
	/*public static void main(String[] args) {
		JSSession s = new JSSession();

		Object r = s.eval("test","a.b(3);", Maps.create("a",(Object)new A()) );
		System.out.println(r);


	}*/
}

