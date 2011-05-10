package jp.tonyu.soytext2.js;



public interface CompileResult {
	//public Object raw();
	public <T> T value(Class<T> type);
	public boolean isUp2Date();
	public DocumentScriptable src();
	public DocumentCompiler compiler();
}
