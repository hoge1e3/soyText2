package jp.tonyu.soytext2.js;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import jp.tonyu.js.BlankScriptableObject;
import jp.tonyu.js.Scriptables;

/**
 *  BlessedScriptable object traces valid prototype of the class(=constructor) 
 *  even when the class was modified.
 *  
 * It also prevents generate extra DocumentScriptable having id and stored to db.
 * 
 * BlessedScriptable can be create by $.inherit(class, initValues) or $.bless(class, initValues);
 * 
 * id=1
 *  $.extend(_,{name:"SPClass",prototype: { foo: function () { return x+1; } });
 * id=2 
 *  $.extend(_ {name:"SBClass",prototype: $.inherit(SPClass, {x:3})});
 * s=new SBClass();  
 * s.foo(); // 4
 * id=1 (modify)
 *  $.extend(_,{name:"SPClass",prototype: { foo: function () { return x+2; } });
 * s.foo(); // 5
 * 
 * SPClass.prototype 
 * 
 * @author shinya
 *
 */
public class BlessedScriptable extends BlankScriptableObject {
	private static final long serialVersionUID = 2800093915277316789L;
	//Function superclass;
	Function klass;
	@Override
	public Scriptable getPrototype() {
		//Function klass = getConstructor();
		Object prot = ScriptableObject.getProperty(klass, Scriptables.PROTOTYPE);
		if (prot instanceof Scriptable) {
			return (Scriptable)prot;
		}
		return null;
	}
	public BlessedScriptable(Function klass) { // klass == superclass
		super();
		this.klass=klass;
		//ScriptableObject.putProperty(this, Scriptables.CONSTRUCTOR, klass);
	}
	@Override
	public String toString() {
		return "(Blessed from "+klass+")";
	}
	/*public Function getConstructor() {
		Object cons=ScriptableObject.getProperty(this, Scriptables.CONSTRUCTOR);
		if (cons instanceof Function) {
			Function s = (Function) cons;
			return s;
		}
		return null;
	}*/
	@Override
	public Object get(String name, Scriptable start) {
		if (Scriptables.CONSTRUCTOR.equals(name)) {
			return klass;
		}
		return super.get(name, start);
	}
	@Override
	public void put(String name, Scriptable start, Object value) {
		if (Scriptables.CONSTRUCTOR.equals(name)) {
			klass=(Function) value;
			return;
		}
		super.put(name, start, value);
	}
	@Override
	public Object[] getIds() {
		Object[] res1=super.getIds();
		Object[] res=new Object[res1.length+1];
		res[0]=Scriptables.CONSTRUCTOR;
		System.arraycopy(res1, 0 , res , 1, res1.length);
		return res;
	}
}
