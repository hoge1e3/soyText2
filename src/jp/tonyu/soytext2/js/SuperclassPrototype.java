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
		return (Scriptable)ScriptableObject.getProperty(superclass, DocumentScriptable.PROTOTYPE);
	}
	public SuperclassPrototype(Function superclass) {
		super();
		this.superclass = superclass;
	}
}
