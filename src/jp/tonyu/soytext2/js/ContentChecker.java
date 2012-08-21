package jp.tonyu.soytext2.js;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import jp.tonyu.debug.Log;
import jp.tonyu.js.BlankFunction;
import jp.tonyu.js.BlankScriptableObject;
import jp.tonyu.js.BuiltinFunc;
import jp.tonyu.js.ContextRunnable;
import jp.tonyu.js.Scriptables;
import jp.tonyu.js.Wrappable;
import jp.tonyu.util.A;
import jp.tonyu.util.MapAction;
import jp.tonyu.util.Maps;
import jp.tonyu.util.SPrintf;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.UniqueTag;

public class ContentChecker implements IDocumentLoader, Wrappable {
	private static final String OK = "OK"	;
	public final String content;
	private StringBuffer changedContent=new StringBuffer();
	String errorMsg;
	@Override
	public Wrappable javaNative(String className) {
		return this;
	}
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
		Set<String> undefinedSymbols = new HashSet<String>();
		undefinedSymbols.addAll(scope.getUndefinedSymbols());
		for (String r: reqs) {
			undefinedSymbols.add(r);
		}
		return undefinedSymbols;
	}
	public String[] reqs;
	private boolean objectInitialized=false, errorOcurred=false,scopeFieldChanged=false;
	//Scriptable initializedObject;
	final private Map<String, String> newVars;
	final private DocumentLoaderScriptable scope;
	boolean syntaxCheckOnly=false;
	public ContentChecker(String content) {
	    this(content, new HashMap<String,String>(),new String[0]);
	    syntaxCheckOnly=true;
	}
	public ContentChecker(String content, Map<String, String> newVars,String[]reqs) {
		super();
		final JSSession jssession = DocumentLoader.curJsSesssion();
		this.content = content;
		this.newVars=newVars;
		this.reqs=reqs;
		scope = new DocumentLoaderScriptable(jssession.root, this, null);
	}

	private Object dummyDocument(Object id) {
		return Scriptables.extend(new BlankFunction(), Maps.create("id", id));
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
				    if (syntaxCheckOnly) {
				        objectInitialized=true;
                        changedContent.delete(0, changedContent.length());
                        changedContent.append(content);
				        cx.compileString(content, "checkSyntax", 1, null);
				        return true;
				    } else {
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
				    }
				} catch (Exception e) {
				    e.printStackTrace();
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
		BlankScriptableObject child = new BlankScriptableObject() {
			@Override
			public Object get(String name, Scriptable start) {
				System.out.println(this+": Get name- start="+start);
				return super.get(name, start);
			}
		};
		BlankScriptableObject prot = new BlankScriptableObject() {
			@Override
			public Object get(String name, Scriptable start) {
				System.out.println(this+": Get name- start="+start);
				return super.get(name, start);
			}
		};
		prot.put("a","b");
		child.setPrototype(prot);
		root.put("test", root, child);
		child.put("s","c");
		Object res=c.evaluateString(root,
				"var buf='';" +
				"for (var x in test) { buf+=x+'='+test[x]+',';" +
				"}; buf", "sourceName", 1, null);
		System.out.println(res);
		/*System.out.println(test.has("a", test));
		System.out.println(test.get("a", test));
		System.out.println(ScriptableObject.getProperty(test,"a"));
		System.out.println(ScriptableObject.getProperty(test,"aa"));*/
		System.out.println(prot.getIds().length);
		System.out.println(child.getIds().length);
	}
	public static void main2(String[] args) {
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
	@Override
	public Scriptable inherit(Function superClass, Scriptable overrideMethods) {
		//overrideMethods.setPrototype((Scriptable)ScriptableObject.getProperty(superClass, DocumentScriptable.PROTOTYPE));
		return overrideMethods;
	}
	@Override
	public Scriptable bless(Function klass, Scriptable fields) {
		return inherit(klass,fields);
	}

}
