package jp.tonyu.soytext2.js;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import jp.tonyu.js.BlankScriptableObject;

public class SuperclassPrototype extends BlankScriptableObject {
	private static final long serialVersionUID = 2800093915277316789L;
	Function superclass;
	@Override
	public Scriptable getPrototype() {
		Object prot = ScriptableObject.getProperty(superclass, DocumentScriptable.PROTOTYPE);
		if (prot instanceof Scriptable) {
			return (Scriptable)prot;
		}
		return null;
	}
	public SuperclassPrototype(Function superclass) {
		super();
		this.superclass = superclass;
	}
	@Override
	public String toString() {
		return "(Superprot for "+superclass+")";
	}
}
