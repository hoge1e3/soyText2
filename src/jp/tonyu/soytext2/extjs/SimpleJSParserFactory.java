package jp.tonyu.soytext2.extjs;

import jp.tonyu.debug.Log;
import jp.tonyu.js.Wrappable;

public class SimpleJSParserFactory implements Wrappable{
	public String parse(String s){
		Log.d(this, "Parsing "+s);
		SimpleJSParser simpleJSParser = new SimpleJSParser(s);
		simpleJSParser.parse();
		String res = simpleJSParser.buf+"";
		Log.d(this,"Returned "+res);
		return res;
	}
}
