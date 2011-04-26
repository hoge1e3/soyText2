package jp.tonyu.soytext.js;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jp.tonyu.soytext2.js.DocumentScriptable;

import org.mozilla.javascript.Scriptable;

import soytext.Document;
import soytext.ParsedDocument;
import soytext.js.HttpContextJSObject;
import soytext.js.RunScript;
import soytext.js.Doc2JS.ContextInfo;
import soytext.parser.Parser;
import soytext.util.Debug;

public class DefaultCompiler {
	class ContextInfo {
		private static final String SCOPE = "scope";
		final public Map<String, Object> consts=new Hashtable<String, Object>();
		final public List<String> paramValues=new Vector<String>();
		public ContextInfo(DocumentScriptable d) {
			Object scope=d.get(SCOPE);
			Object ord=d.get(ARGUMENTORDER);
			if (ord instanceof Scriptable) {
				Scriptable new_name = (Scriptable) ord;
				
			}
			if (scope instanceof Scriptable) {
				Scriptable s = (Scriptable) scope;
				for (String key:pa.keys()) {
					String v=pa.get(key);
					if (v.startsWith("?")) {
						paramValues.add(key);
					} else {
						Object putval = evalValue(d, v);  // evalUnlocked
						if (putval!=null) consts.put(key, putval);		
					}
				}
			}

			HttpContextFunctions.load(consts);

		}
	}
	public Scriptable evalHtmlDocument(Document d)  {
		Debug.syslog("Exec as Html");
		ContextInfo i=new ContextInfo(d);

		String src=d.body();
		Parser p=new Parser(src);
		p.setSpacePattern(null);
		StringBuilder buf=new StringBuilder();
		buf.append("var p=ctx.print;\n");
		buf.append("res=function ("+Parser.join(", ",i.paramValues)+") { \n"); {
			while (true) {
				Debug.syslog("1-"+p.current());
				p.read(htmlPlain);
				//Debug.syslog("1.5-"+p.current());
				buf.append(PRINT+"("+RunScript.literal(p.group())+");\n");
				Debug.syslog("2-"+p.current());
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
		buf.append("};");
		System.out.println("EvalBuf - "+buf);
		Object res=RunScript.eval(buf.toString(), i.consts);
		if (res instanceof Scriptable) {
			Scriptable s = (Scriptable) res;
			return s;
		}
		return null;
	}

}
