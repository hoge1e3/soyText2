package jp.tonyu.js;


import org.mozilla.javascript.ScriptableObject;

public class BlankScriptableObject extends ScriptableObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3588030671162977794L;

	@Override
	public String getClassName() {
		return "Blank";
	}

}