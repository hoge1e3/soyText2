package jp.tonyu.soytext2.extjs;

import jp.tonyu.soytext2.js.CompileResult;
import jp.tonyu.soytext2.js.DocumentCompiler;
import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.servlet.HttpContext;



public class ExtJSCompiler implements DocumentCompiler {

	@Override
	public CompileResult compile(final DocumentScriptable document)  {
		String src=""+document.get(HttpContext.ATTR_BODY);
		final long l=document.getDocument().lastUpdate;
		final ClassConverter c=new ClassConverter(document);
		
		return new CompileResult() {
			
			@Override
			public <T> T value(Class<T> type) {
				if (type.isAssignableFrom(ClassConverter.class)){
					return (T)c;
				}
				return null;
			}
			
			@Override
			public DocumentScriptable src() {
				return document;
			}
			
			@Override
			public boolean isUp2Date() {
				return document.getDocument().lastUpdate==l;
			}
			
			@Override
			public DocumentCompiler compiler() {
				
				return ExtJSCompiler.this;
			}
		};
	}

	

}
