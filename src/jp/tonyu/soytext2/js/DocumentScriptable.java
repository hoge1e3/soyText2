package jp.tonyu.soytext2.js;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jp.tonyu.debug.Log;
import jp.tonyu.js.BuiltinFunc;
import jp.tonyu.js.Scriptables;
import jp.tonyu.js.StringPropAction;
import jp.tonyu.soytext.Origin;
import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.servlet.HttpContext;
import jp.tonyu.util.SPrintf;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.UniqueTag;
import org.omg.CosNaming.NamingContextPackage.NotFound;

public class DocumentScriptable implements Function {
	public static final String PROTOTYPE = "prototype";
	public static final String CONSTRUCTOR = "constructor";
	public static final String CALLSUPER="callSuper";
	private static final Object GETTERKEY = "[[110414_051952@"+Origin.uid+"]]";
	//Scriptable __proto__;
	Map<Object, Object>binds=new HashMap<Object, Object>();
	final DocumentRecord d;
	public final DocumentLoader loader;
	public static final String ONAPPLY="onApply",APPLY="apply",CALL="call";
	public DocumentRecord getDocument() {
		return d;
	}
	public DocumentScriptable(final DocumentLoader loader,final DocumentRecord d) {
		this.loader=loader;
		this.d=d;
		/*put("id",this , d.id );
		put("lastUpdate",this, d.lastUpdate);
		put("save",this, );*/
	}
	BuiltinFunc saveFunc =new BuiltinFunc() {
		
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			save();
			return DocumentScriptable.this;
		}
	};
	BuiltinFunc compileFunc =new BuiltinFunc() {
		
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			CompileResult c = JSSession.cur.get().compile(DocumentScriptable.this);
			/*if (args.length>=1 && args[0]!=null) {
				Log.d(this, args[0]+" -  Class="+args[0].getClass());
			//org.mozilla.javascript.IdFunctionObject
			}*/
			if (c==null) return DocumentScriptable.this;
			if (c instanceof CompileResultScriptable) {
				CompileResultScriptable cs = (CompileResultScriptable) c;
				return cs.scriptable;
			}
			return c;
		}
	};
	BuiltinFunc hasOwnPropFunc= new BuiltinFunc() {
		
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			if (args.length==0) return false;
			return binds.containsKey(args[0]);
		}
	};
	BuiltinFunc callSuperFunc =new BuiltinFunc() {
		
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			Scriptable p = DocumentScriptable.this.getPrototype().getPrototype();
			if (p!=null && args.length>0) {
				Object fo = p.get(args[0]+"", p);
				if (fo instanceof Function) {
					Function f = (Function) fo;
					
					Object[] argShift=new Object[args.length-1];
					for (int i=0 ; i<argShift.length ; i++) {
						argShift[i]=args[i+1];
					}
					return f.call(cx, scope, thisObj, argShift);
				}
			}
			return null;
		}
	};

	public Object get(Object key) {
		if ("id".equals(key)) return d.id;
		if ("lastUpdate".equals(key)) return d.lastUpdate;
		if ("save".equals(key)) return saveFunc;
		//if ("compile".equals(key)) return compileFunc;
		if ("hasOwnProperty".equals(key)) return hasOwnPropFunc;
		if (CALLSUPER.equals(key)) return callSuperFunc;
		/*if (key instanceof DocumentScriptable) {
			DocumentScriptable keyDoc = (DocumentScriptable) key;
			key=JSSession.idref(keyDoc, d.documentSet);
		}*/
		Object res = binds.get(key);
		if (res!=null) return res;
		if (key instanceof DocumentScriptable) {
			DocumentScriptable keyDoc = (DocumentScriptable) key;
			Getter g=keyDoc.getGetter();
			if (g!=null) return g.getFrom(this);
		}	
		/*Scriptable __proto__ = getPrototype();
		if (__proto__!=null) return __proto__.get(key+"",__proto__);*/
		return UniqueTag.NOT_FOUND;
	}
	public Getter getGetter() {
		return (Getter)get(GETTERKEY);
	}
	public void setGetter(Getter g) {
		put(GETTERKEY, g);
	}
	public Object put(Object key,Object value) {
		/*if (key instanceof DocumentScriptable) {
			DocumentScriptable s = (DocumentScriptable) key;			
			binds.put(JSSession.idref(s, d.documentSet),value);
		} else*/
		if (key instanceof String || key instanceof Number) {
			binds.put(key, value);
		} else if (value==null){
			binds.remove(key);
		} else {
			Log.die("Cannot put "+key);
		}
		return value;
	}
	public Set<Object> keySet() {
		return binds.keySet();
	}

	@Override
	public void delete(String name) {
		binds.remove(name);		
	}

	@Override
	public void delete(int index) {
		binds.remove(index);
	}

	@Override
	public Object get(String name, Scriptable start) {
		return get(name);
	}

	@Override
	public Object get(int index, Scriptable start) {
		return get(index);
	}

	@Override
	public String getClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getDefaultValue(Class<?> hint) {
		return toString();
	}

	@Override
	public Object[] getIds() {
		Set<Object> keys=binds.keySet();
		Object[] res=new Object[keys.size()];
		int i=0;
		for (Object key:keys) {
			/*if (key instanceof DocumentScriptable) {
				DocumentScriptable s = (DocumentScriptable) key;
				
				res[i] = JSSession.idref(s, d.documentSet);
				Log.d(this, "Put res["+i+"]="+res[i]);
			} else
			if ("contentEquals".equals(key)) {
				Log.die("Why are you have it? "+this+" "+this.getParentScope()+" "+this.getPrototype());
			}*/
			if (key instanceof String || key instanceof Number) {
				res[i]=key;
			} else {
				Log.die("Wrong key! "+key);
			}
			i++;
		}
		for (Object r:res) {
			Log.d(this ," getids - "+r);
		}
		return res;
	}

	@Override
	public Scriptable getParentScope() {
		// TODO Auto-generated method stub
		return null;
	}
	public Scriptable getConstructor() {
		Object cons = binds.get(CONSTRUCTOR);
		if (cons instanceof Scriptable) {
			Scriptable s = (Scriptable) cons;
			return s;
		}
		return null;
	}
	@Override
	public Scriptable getPrototype() {
		Scriptable s=getConstructor();
		if (s==null) return null;
		Object res=s.get(PROTOTYPE,s);
		if (res instanceof Scriptable) {
			Scriptable ss = (Scriptable) res;
			return ss;
		}
		return null;
	}

	@Override
	public boolean has(String name, Scriptable start) {
		return binds.containsKey(name);
	}

	@Override
	public boolean has(int index, Scriptable start) {
		return binds.containsKey(index);
	}

	@Override
	public boolean hasInstance(Scriptable instance) {
		for (int i=0 ;i<100 ;i++) {
			Object c=ScriptableObject.getProperty(instance, CONSTRUCTOR);
			if (equals(this)) return true;
			if (c instanceof Scriptable) {
				Scriptable cs = (Scriptable) c;
				Object p=ScriptableObject.getProperty(cs, PROTOTYPE);
				if (p instanceof Scriptable) {
					instance = (Scriptable) p;
					continue;
				}
			}
			return false;
		}
		return false;
	}

	@Override
	public void put(String name, Scriptable start, Object value) {
		//if (name.equals("contentEquals")) Log.die("Who set it?");
		put(name,value);
	}

	@Override
	public void put(int index, Scriptable start, Object value) {
		put(index,value);
	}

	@Override
	public void setParentScope(Scriptable parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPrototype(Scriptable prototype) {
		//Log.d(this, "__proto__"+prototype);
		//this.__proto__= prototype;
	}
	public void save() {
		refreshSummary();
		refreshContent();
		Log.d(this, "save() content changed to "+d.content);
		loader.getDocumentSet().save(d);// d.save();
		
	}
	private void refreshContent() {
		Object s=get(HttpContext.ATTR_SCOPE);
		final StringBuilder b=new StringBuilder();
		if (s instanceof Scriptable) {
			Scriptables.each((Scriptable)s, new StringPropAction() {
				
				@Override
				public void run(String key, Object value) {
					if (value instanceof DocumentScriptable) {
						DocumentScriptable dd = (DocumentScriptable) value;
						b.append(SPrintf.sprintf("var %s=$.byId(\"%s\");\n",key,dd.d.id));						
					}
				}
			});
		}
		b.append(SPrintf.sprintf("$.extend(_,%s);",HashLiteralConv.toHashLiteral(this)));
		d.content=b+"";
	}
	public void setContentAndSave(String content) {
		d.content=content;
		Log.d(this, "setContentAndSave() content changed to "+d.content);
		loader.loadFromContent(content, this);		
		refreshSummary();
		loader.getDocumentSet().save(d);//d.save();
	}
	public void reloadFromContent() {
		loader.loadFromContent(d.content, this);		
		refreshSummary();
	}
	@Override
	public String toString() {
		return "(Docscr "+d.id+")";
	}
	public void clear() {
		binds.clear();
	}
	public void refreshSummary() {
		d.summary=genSummary();
		Log.d(this, "Sumamry changed to "+d.summary);
	}
	public String genSummary() {
		Object res;
		res=get("name");
		String ress = res+"";
		if (res!=null && res!=UniqueTag.NOT_FOUND && ress.length()>0) return ress;
		res=get("title");ress = res+"";
		if (res!=null && res!=UniqueTag.NOT_FOUND && ress.length()>0) return ress;
		res=get(HttpContext.ATTR_BODY);ress = res+"";
		if (res!=null && res!=UniqueTag.NOT_FOUND && ress.length()>0) return ress.substring(0,Math.min(ress.length(), 20));
		return d.id;
	}
	@Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj,
            Object[] args) {
		Object r=ScriptableObject.getProperty(this,ONAPPLY);
		if (r instanceof Function) {
			Function f = (Function) r;
			Object[] args2=new Object[] { thisObj ,args };
			return f.call(cx, scope, this, args2);
		}
		r=ScriptableObject.getProperty(this,APPLY);
		if (r instanceof Function) {
			Function f = (Function) r;
			Object[] args2=new Object[] { thisObj ,args };
			return f.call(cx, scope, this, args2);
		}

		r=ScriptableObject.getProperty(this,CALL);
		if (r instanceof Function) {
			Function f = (Function) r;
			Object[] args2=new Object[args.length+1];
			args2[0]=thisObj;
			for (int i=1 ; i<args2.length ;i++){ 
				args2[i]=args[i-1];
			}
			return f.call(cx, scope, this , args2);
		}
		Log.die(this+" is not function-callable.");
		return null;
	}
	@Override
	 public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		DocumentScriptable d=loader.newDocument(null);
		//Scriptable cons = getConstructor();
		d.put(CONSTRUCTOR, this); //cons);
		/*Scriptable p=getPrototype();
		if (p!=null) {*/
			Object init=ScriptableObject.getProperty(d,"initialize");
			Log.d(this, " initialize = "+init);
			if (init instanceof Function) {
				Log.d(this, " initialize called!");
				Function f = (Function) init;
				f.call(cx, scope, d, args);
			} else {
				Log.d(this, " initialize did not called");
				if (init!=null) {
					Log.d(this, "init="+init.getClass().getSuperclass());
				}
			}
		//}
		return d;
	}
}
