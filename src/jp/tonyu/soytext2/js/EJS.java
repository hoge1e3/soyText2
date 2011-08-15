package jp.tonyu.soytext2.js;

import java.util.regex.Pattern;

import jp.tonyu.debug.Log;
import jp.tonyu.js.Wrappable;
import jp.tonyu.parser.Parser;
import jp.tonyu.soytext2.servlet.HttpContext;
import jp.tonyu.util.Literal;

import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;


public class EJS implements Wrappable {
    static  final Pattern htmlPlain=Pattern.compile("([^<]*<[^%])*[^<]*");
	private static final String PRINT = "$.p";
    // static  final Pattern htmlPlain=Pattern.compile("([^<]|(<[^%]))*"); StackOverflow
    final Pattern embedLang=Pattern.compile("<%(=?) *(([^%]|(%[^>]))*)%>");
  
    public Scriptable convert(Scriptable d, Scriptable scope) {
    	//Thread.dumpStack();
		String src=""+ScriptableObject.getProperty(d ,HttpContext.ATTR_BODY );
		JSSession jssession=JSSession.cur.get();
		Parser p=new Parser(src);
		p.setSpacePattern(null);
		StringBuilder buf=new StringBuilder();
		buf.append("res=function ($,params) { \n"); {
  	    	while (true) {
  	    		p.read(htmlPlain);
  	    		buf.append(PRINT+"("+Literal.toLiteral(p.group())+");\n");
  	    		if (p.read(embedLang)) {
  	    			if (p.group(1).length()>0) {
  	    				buf.append(PRINT+"("+p.group(2)+");\n");
  	    			} else {
  	    				buf.append(p.group(2)+";");
  	    			}
  	    		} else {
  	    			break;
  	    		}
  	    	}
		}
		buf.append("}; \n");
//		buf.append("a=(function (a,b) {return a+b;})+\"\"; \n");
//		buf.append("res.str=res+\"\"; \n");
//		buf.append("res;\n");
		System.out.println("EvalBuf - "+buf);
		Object res=jssession.eval("HtmlComp"+d,   buf.toString());
		if (res instanceof Scriptable) {
			Scriptable s = (Scriptable) res;
			//Log.d(this, " Compiled - "+d+" to "+s);
			//Log.d(this, " Compiled - "+d+" to "+ScriptableObject.getProperty(s, "str"));

			//Log.d(this, " Compiled - "+d+" to "+ScriptRuntime.toString(s));
			//Log.d(this, " Compiled - "+d+" to "+ScriptableObject.getDefaultValue(s, String.class));
			return s;
		}
		return null;

	}
}