package jp.tonyu.soytext2.js;

import jp.tonyu.js.Wrappable;

import org.mozilla.javascript.Scriptable;

public interface IDocumentLoader {

	/*private Scriptable instanciator(final DocumentRecord src) {
		return new BlankScriptableObject() {
			private static final long serialVersionUID = -3858849843957575405L;
	
			@Override
			public Object get(String name, Scriptable start) {
				if ("create".equals(name)) { //   src ! create.  (in dolittle)
					return new BuiltinFunc() {
	
						@Override
						public Object call(Context cx, Scriptable scope, Scriptable thisObj,
								Object[] args) {
							DocumentScriptable res = DocumentLoader.this.defaultDocumentScriptable(src);
							if (args.length>=1) {
								res.setPrototype(byId(args[0]+""));
							}
							return res;
						}
					};
				}
				if ("newInstance".equals(name)) { //  new func() 
					return new BuiltinFunc() {
	
						@Override
						public Object call(Context cx, Scriptable scope, Scriptable thisObj,
								Object[] args) {
							DocumentScriptable res = DocumentLoader.this.defaultDocumentScriptable(src);
							if (args.length>=1) {
								DocumentScriptable func = byId(args[0]+"");
								Object proto = func.get("prototype");
								if (proto instanceof Scriptable) {
									Scriptable sc = (Scriptable) proto;
									res.setPrototype(sc);
								}
							}
							return res;
						}
					};
				}
	
				Log.die(name+" not found ");
				return super.get(name, start);
			}
		};
	}*/
	public abstract Object byId(String id);

	public abstract void extend(final DocumentScriptable dst, Scriptable src);
	public Wrappable javaNative(String className);

}