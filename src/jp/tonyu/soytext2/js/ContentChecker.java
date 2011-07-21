package jp.tonyu.soytext2.js;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.UniqueTag;

import jp.tonyu.debug.Log;
import jp.tonyu.js.BlankScriptableObject;
import jp.tonyu.js.BuiltinFunc;
import jp.tonyu.js.ContextRunnable;
import jp.tonyu.util.MapAction;
import jp.tonyu.util.Maps;
import jp.tonyu.util.Ref;

public class ContentChecker {
	String src;
	String msg;
	public boolean syntaxOK=false, objectInitialized=false;
	public ContentChecker(String src) {
		super();
		this.src = src;
	}
	public boolean check() {
		final JSSession jssession = JSSession.cur.get();
		//final Ref<Boolean> res=Ref.create(false);
		JSSession.withContext(new ContextRunnable() {
			
			@Override
			public Object run(Context cx) {
				try {
					//final Set<String> undefinedSymbols=new HashSet<String>();
					undefinedSymbols.clear();
					final Scriptable scope=new BlankScriptableObject(jssession.root) {
						@Override
						public Object get(String name, Scriptable start) {
							Object r = super.get(name, start);
							System.out.println("Get - "+name+" - "+r);
							if (r==UniqueTag.NOT_FOUND) {
								undefinedSymbols.add(name);
								return null;
							}
							return r;
						}
						public void put(String name, Scriptable start, Object value) {
							System.out.println("Put - "+name+" - "+value);
							if (value instanceof Undefined) undefinedSymbols.add(name);
							else {
								if (undefinedSymbols.contains(name)) {
									undefinedSymbols.remove(name);
								}
							}
							super.put(name, start, value);
						}
					};
					Scriptable extender=new BlankScriptableObject();
					extender.put("byId", extender, new BuiltinFunc() {
						@Override
						public Object call(Context cx, Scriptable scope, Scriptable thisObj,
								Object[] args) {
							return args[0];
						}
					});
					extender.put("extend", extender, new BuiltinFunc() {
						@Override
						public Object call(Context cx, Scriptable scope, Scriptable thisObj,
								Object[] args) {
							objectInitialized=true;
							msg="";
							return null;
						}
					});
					scope.put("_", scope, 0);
					scope.put("$", scope, extender);
					msg="Object not inited(Perhaps $.extend did not called)";
					Object result = cx.evaluateString(scope , src, "check" , 1, null);
					//ContentChecker.this.undefinedSymbols.addAll(undefinedSymbols);
					syntaxOK=true;
					return result;
				} catch (Exception e) {
					msg=e.getMessage();
					return null;
				}
			}
		});
		return objectInitialized && undefinedSymbols.isEmpty();
	}
	public final List<String> undefinedSymbols=new Vector<String>();
	public static void main(String[] args) {
		final ContentChecker c = new ContentChecker("var a,c; function () {a=b;}");
		JSSession.cur.enter(new JSSession(),	new Runnable() {
			
			@Override
			public void run() {
				System.out.println(c.check());
				System.out.println(c.undefinedSymbols);
				
			}
		});
	}
	public String getMsg() {
		return msg + (undefinedSymbols.isEmpty()?"":"There are undefined symbols.");
	}
}
