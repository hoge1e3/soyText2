package jp.tonyu.soytext2.js;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jp.tonyu.js.BlankScriptableObject;
import jp.tonyu.js.BuiltinFunc;
import jp.tonyu.js.ContextRunnable;
import jp.tonyu.js.Scriptables;
import jp.tonyu.util.MapAction;
import jp.tonyu.util.Maps;
import jp.tonyu.util.SPrintf;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.UniqueTag;

public class ContentChecker {
	private static final String OK = "OK"	;
	public final String content;
	private StringBuffer changedContent=new StringBuffer();
	String errorMsg;
	public String getChangedContent() {
		return changedContent.toString();
	}
	public boolean isObjectInitialized() {
		return objectInitialized;
	}
	public boolean isErrorOcurred() {
		return errorOcurred;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	public List<String> getUndefinedSymbols() {
		return undefinedSymbols;
	}
	private boolean objectInitialized=false, errorOcurred=false,scopeFieldChanged=false;
	//Scriptable initializedObject;
	final private Map<String, String> newVars;
	public ContentChecker(String content, Map<String, String> newVars) {
		super();
		this.content = content;
		this.newVars=newVars;
	}
	private Object dummyDocument(Object id) {
		return Scriptables.extend(new BlankScriptableObject(), Maps.create("id", id));
	}
	public boolean check() {
		final JSSession jssession = JSSession.cur.get();
		//final Ref<Boolean> res=Ref.create(false);
		JSSession.withContext(new ContextRunnable() {
			
			@SuppressWarnings("serial")
			@Override
			public Object run(Context cx) {
				try {
					//final Set<String> undefinedSymbols=new HashSet<String>();
					undefinedSymbols.clear();
					final BlankScriptableObject tools=new BlankScriptableObject(jssession.root);
					BlankScriptableObject extender=new BlankScriptableObject();
					extender.put("byId", new BuiltinFunc() {
						@Override
						public Object call(Context cx, Scriptable scope, Scriptable thisObj,
								Object[] args) {
							return dummyDocument(args[0]);
						}


					});
					extender.put("extend", new BuiltinFunc() {
						@Override
						public Object call(Context cx, Scriptable scope, Scriptable thisObj,
								Object[] args) {
							objectInitialized=true;
							return null;
						}
					});
					tools.put("_", 0);
					tools.put("$", extender);
					final BlankScriptableObject scope=new BlankScriptableObject(jssession.root) {
						@Override
						public Object get(String name, Scriptable start) {
							Object r = super.get(name, start);
							System.out.println("Get - "+name+" - "+r);
							if (r==UniqueTag.NOT_FOUND && !tools.has(name, tools)) {
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
					scope.setPrototype(tools);
					changedContent.delete(0, changedContent.length());
					Maps.entries(newVars).each(new MapAction<String, String>() {
						
						@Override
						public void run(String key, String value) {
							changedContent.append(SPrintf.sprintf("var %s=%s;\n", key,value));
						}
					});
					changedContent.append(content);
					
					Object result = cx.evaluateString(scope , changedContent.toString(), "check" , 1, null);
					return result;
				} catch (Exception e) {
					errorOcurred=true;
					errorMsg=e.getMessage();
					
					return null;
				}
			}
		});
		return getMsg().equals(OK);
	}
	public final List<String> undefinedSymbols=new Vector<String>();
	public static void main(String[] args) {
		final ContentChecker c = new ContentChecker("var a,c; function () {a=b;}", new HashMap<String, String>());
		JSSession.cur.enter(new JSSession(),	new Runnable() {
			
			@Override
			public void run() {
				System.out.println(c.check());
				System.out.println(c.undefinedSymbols);
				
			}
		});
	}
	public String getMsg() {
		if (errorOcurred) return errorMsg;
		if (!objectInitialized) return "Object not inited(Perhaps $.extend did not called)";
		if (!undefinedSymbols.isEmpty()) return "There are undefined symbols.";
		if (isContentChanged()) return "Content is changed. Confirm again.";
		return OK;
	}
	private boolean isContentChanged() {
		return !content.equals(changedContent.toString());
	}
}
