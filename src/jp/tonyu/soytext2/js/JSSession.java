package jp.tonyu.soytext2.js;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jp.tonyu.debug.Log;
import jp.tonyu.js.ContextRunnable;
import jp.tonyu.js.PrototypeJS;
import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.document.DocumentSet;
import jp.tonyu.util.MapAction;
import jp.tonyu.util.Maps;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class JSSession {
	public static final jp.tonyu.util.Context<JSSession> cur=new jp.tonyu.util.Context<JSSession>();
	public final Scriptable root;
	private Scriptable initObject(Context cx) {
		ScriptableObject o=cx.initStandardObjects();
		String[] builtinsa=new String[] {"String","Boolean","Number","Function","Object","Array","RegExp","undefined","null","true","false"
				,"NaN","Infinity","Date","Math","parseInt","TypeError","InternalError"};
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
		}
		cx.evaluateString(o, PrototypeJS.value, "<prototype>", 1, null);
		return o;
	}
	public JSSession() {
		Context c=Context.enter();
		
		root=initObject(c);
		Context.exit();
	}
	public Object eval (String name, String s) {
		return eval(name, s, new HashMap<String, Object>());
	}
	public Object eval (final String name, final String s , final Scriptable scope) {
		return withContext(new ContextRunnable() {
			@Override
			public Object run(Context cx) {
				try {
					Object result = cx.evaluateString(scope , s, name , 1, null);
					return result;
				} catch (EvaluatorException e) {
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
			cx.setWrapFactory(new SafeWrapFactory());
			return action.run(cx);
		} finally {
			Context.exit();
		}			
	}

	Map<String, CompileResult> compileCache=new HashMap<String, CompileResult>();
	public CompileResult compile(DocumentScriptable d) {
		String id = d.getDocument().id;
		CompileResult compileResult = compileCache.get(id);
		if (compileResult!=null) {
			if (compileResult.isUp2Date()) return compileResult;
		}
		CompileResult res=CompilerResolver.compile(d);
		compileCache.put(id, res);
		return res;
	}
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

