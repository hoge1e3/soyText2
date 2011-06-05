package jp.tonyu.soytext2.js;



public interface CompileResult {
	//public Object raw();
	//public <T> T value(Class<T> type);
	public static final String isUp2DateName = "isUp2Date";
	public boolean isUp2Date();
	public static final String getDocumentSourceName="getDocumentSource";
	public DocumentScriptable getDocumentSource();
	//public DocumentCompiler compiler();
}
