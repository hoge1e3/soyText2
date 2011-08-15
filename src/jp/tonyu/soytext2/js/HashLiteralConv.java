package jp.tonyu.soytext2.js;

import java.util.Scanner;

import jp.tonyu.debug.Log;
import jp.tonyu.js.BuiltinFunc;
import jp.tonyu.util.Maps;
import jp.tonyu.util.Resource;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

public class HashLiteralConv {
	// generated from [[110412_050411]] by [[110222_102732]]
	static Function compiled;
	public static Function compile() {
		if (compiled!=null) return compiled;
		compiled=(Function)JSSession.cur.get().eval("toHashLiteral",
				Resource.text(HashLiteralConv.class, ".js"),
				Maps.create("debug", (Object)debug));
		return compiled;
	}
	static BuiltinFunc debug=new BuiltinFunc() {
		
		@Override
		public Object call(Context cx, Scriptable scope, Scriptable thisObj,
				Object[] args) {
			Log.d("TOHASHLIT", args[0]);
			return null;
		}
	};
	public static String toHashLiteral(Object res) {
		return JSSession.cur.get().call(compile(), new Object[]{res})+"";
	}
	public static void main(String[] args) {
		/*Scanner in= new Scanner(
				HashLiteralConv.class.getResourceAsStream("HashLiteralConv.js")
				);
		while (in.hasNextLine()) {
			System.out.println("> "+in.nextLine());
		}
		in.close();*/
		//System.out.println(value);
		System.out.println("get:"+Resource.text(HashLiteralConv.class, ".js") );
	}
/*	public static final String value=
		"res=function (hash) {\n"+
		" return _self(hash);\n"+
		" function _self(hash) {\n"+
		"   var res=\"{\";\n"+
		"   var com=\"\";\n"+
		"   for (var key in hash) {\n"+
		"     if (hash.hasOwnProperty && !hash.hasOwnProperty(key)) continue;\n"+
		"     var value=hash[key];\n"+
		"     var valueStr=lit(value);\n"+
		"     //if (valueStr!=null) {\n"+
		"       res+=com; com=\",\";\n"+
		"       res+=\"\\\"\"+key+\"\\\": \"+valueStr;\n"+
		"     //}\n"+
		"   }\n"+
		"   return res+\"}\";\n"+
		" }\n"+
		"   function lit(value) {\n"+
		"     if (value && value.id) {\n"+
		"        return document(value);\n"+
		"     } else if (typeof value==\"number\") {\n"+
		"        return value;\n"+
		"     } else if (typeof value==\"boolean\") {\n"+
		"        return value+\"\";\n"+
		"     } else if (typeof value==\"function\") {\n"+
		"        return value+\"\";\n"+
		"     } else if (typeof value==\"string\") {\n"+
		"        return str(value);\n"+
		"     } else if (typeof value==\"object\") {\n"+
		"        if (value instanceof Array) {\n"+
		"           return ary(value);\n"+
		"        } else {\n"+
		"           return _self(value);\n"+
		"        }\n"+
		"     } else if (value==null) {\n"+
		"        return \"null\";\n"+
		"     } else {\n"+
		"        return \"null\";\n"+
		"     }  \n"+
		"   }\n"+
		"   function document(d) {\n"+
		"     return \"$.byId(\\\"\"+d.id+\"\\\")\";\n"+
		"   }\n"+
		"   function str(s) {\n"+
		"     s=s.replace(/\\\\/g,\"\\\\\\\\\")\n"+
		"        .replace(/\\n/g,\"\\\\n\")\n"+
		"        .replace(/\\r/g,\"\\\\r\")\n"+
		"        .replace(/\\\"/g,\"\\\\\\\"\");\n"+
		"     return \"\\\"\"+s+\"\\\"\";\n"+
		"   }\n"+
		"   function ary(s) {\n"+
		"     return \"[\"+s.map(lit).join(\", \")+\"]\";\n"+
		"   }\n"+
		"};";
*/
	/*	public static String toHashLiteral(Scriptable s) {
		String res="{";
		String com="";
		for (var key in hash) {
			var value=hash[key];
			res+=com; com=",";
			res+="\""+key+"\": "+lit(value);
		}
		return res+"}";
	}

	static String lit(Object value) {
		if (value instanceof DocumentScriptable) {
			return document((DocumentScriptable)value);
		} else if (value instanceof Integer || value instanceof Boolean) {
			return value+"";
		} else if (value instanceof String) {
			return str(""+value);
		} else if (typeof value=="object") {
			if (value instanceof Array) {
				return ary(value);
			} else {
				return toHashLiteral(value);
			}
		} else if (value==null) {
			return "null";
		} else {
			return "NOT COMVERT"+value;
		}  
	}
	static String document(DocumentScriptable d) {
		return "$.byId(\""+d.getDocument().id+"\")";
	}
	static String ary(Object[] s) {
		return "["+s.map(lit).join(", ")+"]";
	}
	 */
	/*public static void main(String[] args) {
		JSSession.cur.enter(new JSSession(), new Runnable() {
			
			@Override
			public void run() {
				JSSession jsSession = JSSession.cur.get();
				Object res=jsSession.eval("r={a:3, b:[2,3]};");
				System.out.println(toHashLiteral(res));
				
			}
		});
	}*/
}
