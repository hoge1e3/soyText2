package jp.tonyu.soytext2.js;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import jp.tonyu.debug.Log;
import jp.tonyu.js.BlankScriptableObject;
import jp.tonyu.js.BuiltinFunc;
import jp.tonyu.js.ContextRunnable;
import jp.tonyu.js.Scriptables;
import jp.tonyu.js.Wrappable;
import jp.tonyu.util.MapAction;
import jp.tonyu.util.Maps;
import jp.tonyu.util.SPrintf;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.UniqueTag;

public class ContentChecker implements IDocumentLoader, Wrappable {
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
	public Set<String> getUndefinedSymbols() {
		return scope.getUndefinedSymbols();
	}
	private boolean objectInitialized=false, errorOcurred=false,scopeFieldChanged=false;
	//Scriptable initializedObject;
	final private Map<String, String> newVars;
	final private DocumentLoaderScriptable scope;
	public ContentChecker(String content, Map<String, String> newVars) {
		super();
		final JSSession jssession = JSSession.cur.get();
		this.content = content;
		this.newVars=newVars;
		scope = new DocumentLoaderScriptable(jssession.root, this, null);
	}
	private Object dummyDocument(Object id) {
		return Scriptables.extend(new BlankScriptableObject(), Maps.create("id", id));
	}
	@Override
	public void extend(DocumentScriptable dst, Scriptable src) {
		objectInitialized=true;
		
	}
	@Override
	public Object byId(String id) {
		return dummyDocument(id);
	}
	public boolean check() {
		//final Ref<Boolean> res=Ref.create(false);
		JSSession.withContext(new ContextRunnable() {
			
			@SuppressWarnings("serial")
			@Override
			public Object run(Context cx) {
				try {
					//final Set<String> undefinedSymbols=new HashSet<String>();
					//undefinedSymbols.clear();
					//final BlankScriptableObject tools=new BlankScriptableObject(jssession.root);
					//BlankScriptableObject extender=new BlankScriptableObject();
					/*extender.put("byId", new BuiltinFunc() {
						@Override
						public Object call(Context cx, Scriptable scope, Scriptable thisObj,
								Object[] args) {
							return dummyDocument(args[0]);
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
					scope.setPrototype(tools);*/
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
	public String getMsg() {
		if (errorOcurred) return errorMsg;
		if (!objectInitialized) return "Object not inited(Perhaps $.extend did not called)";
		if (!getUndefinedSymbols().isEmpty()) return "There are undefined symbols.";
		if (isContentChanged()) return "Content is changed. Confirm again.";
		return OK;
	}
	private boolean isContentChanged() {
		return !content.equals(changedContent.toString());
	}
	//public final List<String> undefinedSymbols=new Vector<String>();
	/*public static void main(String[] args) {
		final ContentChecker c = new ContentChecker("var a,c; function () {a=b;}", new HashMap<String, String>());
		JSSession.cur.enter(new JSSession(),	new Runnable() {
			
			@Override
			public void run() {
				System.out.println(c.check());
				System.out.println(c.undefinedSymbols);
				
			}
		});
	}*/
	public static void main(String[] args) {
		Context c=Context.enter();
		final ScriptableObject root = c.initStandardObjects();
		BlankScriptableObject scope1 = new BlankScriptableObject(root) {
			@Override
			public void put(String name, Scriptable start, Object value) {
				Log.d("main:cont", name+"="+value);
				super.put(name, start, value);
			}
		};
		Scriptable scope2=new Scriptable() {
			
			@Override
			public void setPrototype(Scriptable prototype) {
				// TODO Auto-generated method stub
				Log.d("main:cont", "scope2:");
				
			}
			
			@Override
			public void setParentScope(Scriptable parent) {
				// TODO Auto-generated method stub
				Log.d("main:cont", "Set Parent"+parent);
			}
			
			@Override
			public void put(int index, Scriptable start, Object value) {
				// TODO Auto-generated method stub
				Log.d("main:cont", "scope2:");
			
			}
			
			@Override
			public void put(String name, Scriptable start, Object value) {
				// TODO Auto-generated method stub
				Log.d("main:cont2", name+"="+value);
				
			}
			
			@Override
			public boolean hasInstance(Scriptable instance) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean has(int index, Scriptable start) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean has(String name, Scriptable start) {
				Log.d("main:cont", "scope2:has");

				return false;
			}
			
			@Override
			public Scriptable getPrototype() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Scriptable getParentScope() {
				// TODO Auto-generated method stub
				return root;
			}
			
			@Override
			public Object[] getIds() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Object getDefaultValue(Class<?> hint) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getClassName() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Object get(int index, Scriptable start) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Object get(String name, Scriptable start) {
				// TODO Auto-generated method stub
				Log.d("main:cont", "get - "+name+"="+start);
				return null;
			}
			
			@Override
			public void delete(int index) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void delete(String name) {
				// TODO Auto-generated method stub
				
			}
		};
		Scriptable scope3 = new DocumentLoaderScriptable(root, null, null);
		c.evaluateString(scope1, "var t=3;"	, "test", 1, null);
		c.evaluateString(scope2, "var t=3;"	, "test", 1, null);
		c.evaluateString(scope3, "var t=3;"	, "test", 1, null);
		
	}

}
