package jp.tonyu.soytext2.js;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jp.tonyu.debug.Log;
import jp.tonyu.soytext.js.PrototypeJS;
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

	@SuppressWarnings("serial")
	public Object eval (String s , Map<String,Object> scope) {
		final Context cx = Context.enter();
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
			cx.setWrapFactory(new SafeWrapFactory());
			Object result = cx.evaluateString(s2 , s, "<cmd>", 1, null);
			return result;
		} catch (EvaluatorException e) {
			Log.die("JS -error at ||"+e.lineSource()+"|| "+e.details());
			return null;
		} finally {
			Context.exit();
		}
	}
	public Object call(Function f, Object[] args) {
		Context cx = Context.enter();
		try {
			cx.setWrapFactory(new SafeWrapFactory());
			Object result = f.call(cx, root, root, args);;
			return result;
		} finally {
			Context.exit();
		}
	}

	public void install(DocumentScriptable d) {
		Object res=CompilerResolver.compile(d);
		root.put(idref(d), root, res);
	}
	private String idref(DocumentScriptable d) {
		return "[["+d.getDocument().id+"]]";
	}
	public static void main(String[] args) {
		JSSession s = new JSSession();
		Object r = s.eval("parseInt(x)+3;", Maps.create("x",(Object)"123") );
		System.out.println(r);


		r = s.eval("parseInt(y)+3;", Maps.create("y",(Object)"15") );
		System.out.println(r);
	}
}
