package jp.tonyu.soytext2.js;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jp.tonyu.debug.Log;
import jp.tonyu.js.PrototypeJS;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;


public class RunScript {
	 static Scriptable initObjCache=null;
	 public static Scriptable initObject(Context cx) {
		 if (initObjCache!=null) return initObjCache;
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
		 initObjCache=o;
		 return o;
	 }

	 public static Object eval (String s, Map<String , Object> vars) {
         Context cx = Context.enter();
         try {
        	  Scriptable scope=initObject(cx);
          	
             for (String k:vars.keySet()) {
                 scope.put(k, scope, vars.get(k));
             }
             cx.setWrapFactory(new SafeWrapFactory());
             Object result = cx.evaluateString(scope, s, "<cmd>", 1, null);
             return result;
         } catch (EvaluatorException e) {
        	 Log.die("JS -error at ||"+e.lineSource()+"|| "+e.details());
        	 return null;
         } finally {
             Context.exit();
         }
	 }
	 public static Object call(Function f, Object[] args) {
         Context cx = Context.enter();
         try {
     		 cx.setWrapFactory(new SafeWrapFactory());
        	 Scriptable scope=initObject(cx);
             Object result = f.call(cx, scope, scope, args);;
             return result;
         } finally {
             Context.exit();
         }
	 }
     public static String literal(String raw) {
    	 String cook=raw;
    	 cook=cook.replaceAll("\\\\", "\\\\\\\\");
    	 cook=cook.replaceAll("\\n","\\\\n");
    	 cook=cook.replaceAll("\\r","\\\\r");
    	 cook=cook.replaceAll("'","\\\\'");
    	 return "'"+cook+"'";
     }
 }
