package jp.tonyu.soytext2.js;

import jp.tonyu.js.Wrappable;

import org.mozilla.javascript.Scriptable;

public interface IDocumentLoader {

	public abstract Object byId(String id);
	public abstract void extend(final DocumentScriptable dst, Scriptable src);
	public Wrappable javaNative(String className);
	//public Object find(Object expr);
}