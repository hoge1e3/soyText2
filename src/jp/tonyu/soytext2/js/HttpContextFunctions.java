package jp.tonyu.soytext2.js;

import java.io.IOException;
import java.util.Map;

import jp.tonyu.debug.Log;
import jp.tonyu.js.BuiltinFunc;
import jp.tonyu.js.Scriptables;
import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.servlet.HTMLDecoder;
import jp.tonyu.soytext2.servlet.HttpContext;
import jp.tonyu.util.Ref;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;


public class HttpContextFunctions  {
	public static final String 
	  PRINT="p",SAVE="save",SEARCH="search",
	  EXECLINK="execLink",EXECPATH="execPath",HTMLENCODE="h",
	  USER="user",BYID="byId",PARAMS="params",
	  ROOTPATH="rootPath",
	  REDIRECT="redirect",NEWINSTANCE="newInstance",HEADER="header";
	
	public static void load(Map<String,Object> to) {
		to.put(PRINT, printFunc);
		to.put(SAVE, saveFunc);
		to.put(SEARCH, searchFunc);
		to.put(EXECLINK,execLinkFunc);
		to.put(EXECPATH, execPathFunc);
		to.put(HTMLENCODE, htmlEncodeFunc);
		to.put(USER,userFunc);
		to.put(BYID,byIdFunc);
		to.put(PARAMS,paramsFunc);
		to.put(ROOTPATH,rootPathFunc);
		to.put(REDIRECT,redirectFunc);
		to.put(NEWINSTANCE,newInstanceFunc);
		to.put(HEADER,headerFunc);
	}
	public static HttpContext c() {
		return HttpContext.cur.get();
	}
	static BuiltinFunc headerFunc=new BuiltinFunc() {
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			HttpContext ctx=c();
			if (args.length>=2) {
				ctx.getRes().setHeader(args[0]+"", args[1]+"");
			}
			return thisObj;
		}
	};
	static BuiltinFunc printFunc=new BuiltinFunc() {
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			HttpContext ctx=c();
			if (args.length>0) {
				try {
					ctx.print(args[0]);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return thisObj;
		}
	};
	static BuiltinFunc searchFunc=new BuiltinFunc() {			
		@SuppressWarnings("serial")
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			final String cond;
			final Scriptable tmpl;
			if (args.length>0 && args[0] instanceof String) {
				cond = (String) args[0];
			} else cond="";
			if (args.length>1 && args[1] instanceof Scriptable) {
				tmpl = (Scriptable) args[1];					
			} else tmpl=null;
			return new ScriptableObject() {
				@Override
				public String getClassName() {
					return "Searcher";
				}
				public Object get(String name, Scriptable start) {
					if ("each".equals(name)) {
						return new BuiltinFunc() {
							
							@Override
							public Object call(Context cx, Scriptable scope, Scriptable thisObj,
									Object[] args) {
								if (args.length>0 && args[0] instanceof Function) {
									Function iter=(Function)args[0];
									c().documentLoader.search(cond, tmpl, iter);
								}
								return null;
							}
						};
					}
					if ("find1".equals(name)) {
						return new BuiltinFunc() {

							@Override
							public Object call(Context cx, Scriptable scope,
									Scriptable thisObj, Object[] args) {
								final Ref<Object> res=new Ref<Object>();
								c().documentLoader.search(cond, tmpl, new BuiltinFunc() {
									
									@Override
									public Object call(Context cx, Scriptable scope, Scriptable thisObj,
											Object[] args) {
										res.set(args[0]);
										return null;
									}
								});
								
								return res.get();
							}
							
						};
					}
					Log.die("Function "+name+" not found.");
					return super.get(name, start);
				}
			};
			
		}

	};
	public static DocumentScriptable makePersistent(DocumentLoader loader, Scriptable obj) {
		DocumentScriptable s=loader.newDocument(obj);
		return s;
	}
	
	public static DocumentScriptable save(Scriptable obj) {
		if (obj instanceof DocumentScriptable) {
			DocumentScriptable d = (DocumentScriptable) obj;
			d.save();
			return d;
		}
		HttpContext hctx=c();
		DocumentScriptable s = makePersistent(hctx.documentLoader, obj);
		s.save();
		return s;
	}
	static BuiltinFunc saveFunc=new BuiltinFunc() {
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			if (args.length>0 && args[0] instanceof Scriptable) {
				return save((Scriptable)args[0]);					
			}
			return thisObj;
		}
	};
	static BuiltinFunc execLinkFunc=new BuiltinFunc() {
		/*private String toId(DocumentScriptable r) {
			return r.get("id").toString();
		}*/
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			return "\""+HTMLDecoder.encode(execPathFunc.call(cx, scope, thisObj, args)+"")+"\"";
			/*if (args.length>0 && args[0] instanceof Scriptable) {
				Scriptable es=(Scriptable)args[0];
				Object es2 = es.get(DefaultCompiler.ATTR_SRC, es);
				Log.d("execLink", es2);
				DocumentScriptable eref = (DocumentScriptable) es2;
				StringBuilder b=new StringBuilder( c().rootPath()+"/exec/"+HTMLDecoder.encode(toId(eref))+"?" );
				if (args.length>=2 && args[1] instanceof Scriptable) {
					Scriptable scr = (Scriptable) args[1];
					for (Object k:scr.getIds()) {
						if (k instanceof String) {
							String s = (String) k;
							Object val=scr.get(s,scr);
							String sval;
							if (val instanceof DocumentScriptable) {
								DocumentScriptable ref = (DocumentScriptable) val;
								sval=toId(ref);
							} else {
								sval=val.toString();
							}
							b.append(HTMLDecoder.encode(s)+"="+HTMLDecoder.encode(sval)+"&");
						}
					}
				}
				Log.d("execLink", b);
				return "\""+ b + "\"";			
			}
			return Log.die("Not scriptable in first arg: execLink");*/
		}
	};
	static BuiltinFunc execPathFunc=new BuiltinFunc() {
		private String toId(DocumentScriptable r) {
			return r.get("id").toString();
		}
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			if (args.length>0 && args[0] instanceof Scriptable) {
				Scriptable es=(Scriptable)args[0];
				DocumentScriptable eref;
				if (es instanceof DocumentScriptable) {
					eref = (DocumentScriptable) es;
				} else {
					Object gsrc = es.get(CompileResult.getDocumentSourceName, es);
					Log.d(this, gsrc);
					Function gsrcf=(Function) gsrc;
					Object gsrcr = gsrcf.call(cx, scope, es, new Object[0]);
					Log.d(this, gsrcr);
					eref = (DocumentScriptable) gsrcr;
				}
				StringBuilder b=new StringBuilder( c().rootPath()+"/exec/"+(toId(eref))+"?" );
				if (args.length>=2 && args[1] instanceof Scriptable) {
					Scriptable scr = (Scriptable) args[1];
					for (Object k:scr.getIds()) {
						if (k instanceof String) {
							String s = (String) k;
							Object val=scr.get(s,scr);
							String sval;
							if (val instanceof DocumentScriptable) {
								DocumentScriptable ref = (DocumentScriptable) val;
								sval="[["+toId(ref)+"]]";
							} else {
								sval=val.toString();
							}
							b.append((s)+"="+(sval)+"&");
						}
					}
				}
				return b.toString();			
			}
			return Log.die("Not scriptable in first arg: execLink");
		}
	};

	static BuiltinFunc htmlEncodeFunc=new BuiltinFunc() {
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			if (args.length==0) return "";
			return HTMLDecoder.encode(args[0].toString());
		}
	};
	
	static BuiltinFunc userFunc=new BuiltinFunc() {
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
		//	return c().currentSession().id();
			return "defaultSession";
		}
	};
	static BuiltinFunc paramsFunc=new BuiltinFunc() {
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			Scriptable res=new ScriptableObject() {
				private static final long serialVersionUID = 5951736573803599511L;
				@Override
				public String getClassName() {	
					return "Params";
				}
			};
			if (args.length>0 && args[0] instanceof Scriptable) {
				Map<String, Object> m=c().params(Scriptables.toStringKeyMap((Scriptable)args[0]));
				Scriptables.extend(res, m);				
			} else {
				Map<String, String> m=c().params();
					/*for (String k:m.keySet()) {
					res.put(k, res, m.get(k));
				}*/
				Scriptables.extend(res, m);
			}
			return res;
		}
	};
	static BuiltinFunc byIdFunc=new BuiltinFunc() {
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			if (args.length>0 && args[0]!=null) 	{
				return c().documentLoader.byId(args[0].toString());
			} 
			return null;
		}
	};

	static BuiltinFunc rootPathFunc=new BuiltinFunc() {
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			return c().rootPath();
		}
	};
	static public BuiltinFunc redirectFunc=new BuiltinFunc() {
		
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			c().redirect(args[0].toString());
			return null;
		}
	};

	static public BuiltinFunc newInstanceFunc=new BuiltinFunc() {
		
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			//Properties p=c().applicationContext().getConfig("allowedJavaClass");
			if (args.length==0) return null;
			String className=args[0].toString();
			try {
				return ((Wrappable)Class.forName(className).newInstance());
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			return null;
		}
	};


}
