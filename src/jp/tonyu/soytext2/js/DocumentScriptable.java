package jp.tonyu.soytext2.js;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jp.tonyu.db.NotInWriteTransactionException;
import jp.tonyu.debug.Log;
import jp.tonyu.js.AllPropAction;
import jp.tonyu.js.BlankScriptableObject;
import jp.tonyu.js.BuiltinFunc;
import jp.tonyu.js.Scriptables;
import jp.tonyu.js.StringPropAction;
import jp.tonyu.soytext.Origin;
import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.document.DocumentSet;
import jp.tonyu.soytext2.document.IndexRecord;
import jp.tonyu.soytext2.document.PairSet;
import jp.tonyu.soytext2.file.AttachedBinData;
import jp.tonyu.soytext2.file.BinData;
import jp.tonyu.soytext2.file.FileSyncer;
import jp.tonyu.soytext2.servlet.HttpContext;
import jp.tonyu.util.SFile;
import jp.tonyu.util.SPrintf;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.UniqueTag;
import org.omg.CosNaming.NamingContextPackage.NotFound;

public class DocumentScriptable implements Function {
	public static boolean lazyLoad=true;
	boolean contentLoaded=!lazyLoad; // true iff loaded or loading
	private void loadContent() {
		if (contentLoaded) return;
		contentLoaded=true;
		reloadFromContent();
	}
	public static final String IS_INSTANCE_ON_MEMORY = "isInstanceOnMemory";
	//public static final String PROTOTYPE = "prototype";
	//public static final String CONSTRUCTOR = "constructor";
	public static final String CALLSUPER="callSuper";
	private static final Object GETTERKEY = "[[110414_051952@"+Origin.uid+"]]";
	//Scriptable __proto__;
	Map<Object, Object>_binds=new HashMap<Object, Object>();
	final DocumentRecord d;
	public final DocumentLoader loader;
	public static final String ONAPPLY="onApply",APPLY="apply",CALL="call";
	private static final Object SETCONTENTANDSAVE = "setContentAndSave";
	private static final Object GETCONTENT = "getContent";
	public DocumentRecord getDocument() {
		return d;
	}
	private Map<Object, Object> binds() {
		loadContent();
		return _binds;
	}
	//static Map<String, DocumentScriptable> debugH=new HashMap<String, DocumentScriptable>();
	public DocumentScriptable(final DocumentLoader loader,final DocumentRecord d) {
		this.loader=loader;
		this.d=d;
		if (d.content==null) contentLoaded=true;
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
	BuiltinFunc setContentAndSaveFunc = new BuiltinFunc() {
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			setContentAndSave(args[0]+"");
			return DocumentScriptable.this;
		}
	};
	BuiltinFunc getContentFunc = new BuiltinFunc() {
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			return d.content;
		}
	};
	/*BuiltinFunc compileFunc =new BuiltinFunc() {

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			CompileResult c = DocumentLoader.curJsSesssion().compile(DocumentScriptable.this);
			if (c==null) return DocumentScriptable.this;
			if (c instanceof CompileResultScriptable) {
				CompileResultScriptable cs = (CompileResultScriptable) c;
				return cs.scriptable;
			}
			return c;
		}
	};*/
	BuiltinFunc hasOwnPropFunc= new BuiltinFunc() {

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			if (args.length==0) return false;
			return binds().containsKey(args[0]);
		}
	};
	BuiltinFunc getBlobFunc= new BuiltinFunc() {

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			loadContent();
			SFile f = FileSyncer.getBlobFile(loader.getDocumentSet(), DocumentScriptable.this);
			return new AttachedBinData(f);
		}
	};
	BuiltinFunc setBlobFunc= new BuiltinFunc() {

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			if (args.length==0) return false;
			loadContent();
			InputStream  str=null;
			if (args[0] instanceof InputStream) {
				str = (InputStream) args[0];
			}
			if (args[0] instanceof BinData) {
				BinData b = (BinData) args[0];
				try {
					str= b.getInputStream();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (str!=null) {
				try {
					FileSyncer.setBlob(DocumentScriptable.this, str);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
	};
	int callsuperlim=0;
	BuiltinFunc callSuperFunc =new BuiltinFunc() {

		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			loadContent();
			if (args.length>0) {
				int c=0;
				String name=args[0]+"";
				for (Scriptable p=DocumentScriptable.this;p!=null ; p=p.getPrototype()) {
					Object fo=p.get(name, p);
					if (fo instanceof Function) {
						c++;
						if (c==2) {
							Function f = (Function) fo;

							Object[] argShift=new Object[args.length-1];
							for (int i=0 ; i<argShift.length ; i++) {
								argShift[i]=args[i+1];
							}
							Log.d(this, "Calling superclass function "+cx.decompileFunction(f,0));
							return f.call(cx, scope, thisObj, argShift);
						}
					}
				}
			}/*
			Scriptable proto = DocumentScriptable.this.getPrototype();
			Log.d(this, "proto = "+proto);
			callsuperlim++;if (callsuperlim>10) {
				callsuperlim=0;Log.die("stackover?");
			}
			Scriptable p = proto.getPrototype();
			Log.d(this, "proto.p = "+p);
			if (p!=null && args.length>0) {

				Object fo = p.get(args[0]+"", p);
				if (fo instanceof Function) {
					Function f = (Function) fo;

					Object[] argShift=new Object[args.length-1];
					for (int i=0 ; i<argShift.length ; i++) {
						argShift[i]=args[i+1];
					}
					Log.d(this, "Calling superclass function "+cx.decompileFunction(f,0));
					return f.call(cx, scope, thisObj, argShift);
				}
			}*/
			return null;
		}
	};

	public Object get(Object key) {
		if ("id".equals(key)) return d.id;
		if (DocumentRecord.LASTUPDATE.equals(key)) return d.lastUpdate;
		if (DocumentRecord.OWNER.equals(key)) return d.owner;
		if ("summary".equals(key)) return d.summary;
		if ("identityHashCode".equals(key)) return System.identityHashCode(this);
		if ("save".equals(key)) return saveFunc;
		//if ("compile".equals(key)) return compileFunc;
		if ("hasOwnProperty".equals(key)) return hasOwnPropFunc;
		if ("setBlob".equals(key)) return setBlobFunc;
		if ("getBlob".equals(key)) return getBlobFunc;
		if (SETCONTENTANDSAVE.equals(key)) return setContentAndSaveFunc;
		if (GETCONTENT.equals(key)) return getContentFunc;
		if (CALLSUPER.equals(key)) return callSuperFunc;
		/*if (key instanceof DocumentScriptable) {
			DocumentScriptable keyDoc = (DocumentScriptable) key;
			key=JSSession.idref(keyDoc, d.documentSet);
		}*/
		Object res = binds().get(key);
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
			binds().put(key, value);
		} else if (value==null){
			binds().remove(key);
		} else {
			Log.die("Cannot put "+key);
		}
		return value;
	}
	public Set<Object> keySet() {
		return binds().keySet();
	}

	@Override
	public void delete(String name) {
		binds().remove(name);
	}

	@Override
	public void delete(int index) {
		binds().remove(index);
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
		Set<Object> keys=binds().keySet();
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
		Object cons = binds().get(Scriptables.CONSTRUCTOR);
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
		Object res=s.get(Scriptables.PROTOTYPE,s);
		if (res instanceof Scriptable) {
			Scriptable ss = (Scriptable) res;
			return ss;
		}
		return null;
	}

	@Override
	public boolean has(String name, Scriptable start) {
		return binds().containsKey(name);
	}

	@Override
	public boolean has(int index, Scriptable start) {
		return binds().containsKey(index);
	}

	@Override
	public boolean hasInstance(Scriptable instance) {
		for (int i=0 ;i<100 ;i++) {
			Object c=ScriptableObject.getProperty(instance, Scriptables.CONSTRUCTOR);
			if (equals(this)) return true;
			if (c instanceof Scriptable) {
				Scriptable cs = (Scriptable) c;
				Object p=ScriptableObject.getProperty(cs, Scriptables.PROTOTYPE);
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
		PairSet<String,String> updatingIndex = indexUpdateMap();
		loader.save(d, updatingIndex);
		//loader.getDocumentSet().save(d,updatingIndex);// d.save();
	}
	private PairSet<String,String> indexUpdateMap() {
		PairSet<String,String> updatingIndex=new PairSet<String,String>();
		updateIndex(updatingIndex);
		Log.d("updateIndex", "save() - index set to "+updatingIndex);
		return updatingIndex;
	}
	private void updateIndex(PairSet<String,String> idx) {
		String name = Scriptables.getAsString(this, "name", null);
		if (name!=null) idx.put("name", name);
		updateClassIndex(idx);
		updateBackLinkIndex(this , idx);
	}
	private void updateClassIndex(	PairSet<String, String> idx) {
		int depth=0;
		for (Function klass=Scriptables.getClass(this);
		     klass!=null;
		     klass=Scriptables.getSuperclass(klass)
		) {
			if (klass instanceof DocumentScriptable) {
				DocumentScriptable d = (DocumentScriptable) klass;
				idx.put(IndexRecord.INDEX_INSTANCEOF, d.id());
			} else {
				break;
			}
			if (depth++>16) Log.die("Depth too many");
		}
	}
	public String id() {
		return getDocument().id;
	}
	private static void updateBackLinkIndex(final Scriptable s, final PairSet<String,String> idx) {
		if (s instanceof NativeJavaObject) return;
		Scriptables.each(s, new AllPropAction() {
			@Override
			public void run(Object key, Object value) {
				//Log.d("updateIndex", key+"="+value);
				if (value instanceof DocumentScriptable) {
					Log.d("updateIndex", s+"put "+key+"="+value);
					DocumentScriptable d = (DocumentScriptable) value;
					idx.put(IndexRecord.INDEX_REFERS, d.getDocument().id);
				} else 	if (value instanceof Scriptable) {
					Scriptable scr = (Scriptable) value;
					updateBackLinkIndex(scr,idx);
				}
			}
		});
	}
	private void refreshContent() {
		//Object s=get(HttpContext.ATTR_SCOPE);
		final StringBuilder b=new StringBuilder();
		/*if (s instanceof Scriptable) {
			Scriptables.each((Scriptable)s, new StringPropAction() {

				@Override
				public void run(String key, Object value) {
					if (value instanceof DocumentScriptable) {
						DocumentScriptable dd = (DocumentScriptable) value;
						b.append(SPrintf.sprintf("var %s=$.byId(\"%s\");\n",key,dd.d.id));
					}
				}
			});
		}*/
		//b.append(SPrintf.sprintf("$.extend(_,%s);",HashLiteralConv.toHashLiteral(this)));
		b.append(HashLiteralConv.toHashLiteral(this));
		d.content=b+"";
	}
	public void setContentAndSave(String content) {
		d.content=content;
		if (d.content==null) Log.die("Content of "+d.id+" is null!");
		String c=d.content;
		if (c.length()>10000) c=c.substring(0,10000);
		Log.d(System.identityHashCode(this), "setContentAndSave() content changed to "+c);
		loader.loadFromContent(content, this);
		refreshSummary();
		PairSet<String,String> idx = indexUpdateMap();
		loader.save(d, idx);
		//loader.getDocumentSet().save(d, idx);//d.save();
	}
	public void reloadFromContent() {
		if (d.content==null) Log.die("Content of "+d.id+" is null!");
		loader.loadFromContent(d.content, this);
		refreshSummary();
	}
	@Override
	public String toString() {
		return "(Docscr "+d.id+")";
	}
	public void clear() {
		binds().clear();
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
	public boolean isInstanceOnMemory() {
		Object r=get(IS_INSTANCE_ON_MEMORY);
		if (r instanceof Boolean) {
			return (Boolean)r;
		}
		return false;
	}
	@Override
	 public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
		Scriptable d; //  generate id
		if (isInstanceOnMemory()) {
			d=new BlessedScriptable(this);
			/* new BlankScriptableObject();
			Object prot = get(Scriptables.PROTOTYPE);
			if (prot instanceof Scriptables) {
				d.setPrototype( (Scriptable)prot  );
			}*/
		} else {
			d=loader.newDocument(); //  generate id
		}
		//Scriptable cons = getConstructor();
		ScriptableObject.putProperty(d,Scriptables.CONSTRUCTOR, this); //cons);
		String name=Scriptables.getAsString(this, "name", null);
		if (name!=null) {
			Scriptable scope2=new BlankScriptableObject();
			ScriptableObject.putProperty(scope2, name, this);
			ScriptableObject.putProperty(d, HttpContext.ATTR_SCOPE, scope2);
		}
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
	public void refreshIndex() throws NotInWriteTransactionException {
		PairSet<String,String> h = indexUpdateMap();
		loader.getDocumentSet().updateIndex(getDocument(), h);
	}
}
