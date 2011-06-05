package jp.tonyu.soytext2.servlet;

import java.util.Map;

import org.mozilla.javascript.Scriptable;

import jp.tonyu.soytext2.js.CompileResultScriptable;

public abstract class SWebApplication extends CompileResultScriptable {
	public SWebApplication(Scriptable scriptable) {
		super(scriptable);
	}

	public abstract void run();
}
