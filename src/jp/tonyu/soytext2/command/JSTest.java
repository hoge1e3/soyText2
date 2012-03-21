package jp.tonyu.soytext2.command;
import java.io.File;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.tmatesoft.sqljet.core.SqlJetException;

import jp.tonyu.js.BuiltinFunc;
import jp.tonyu.soytext.Origin;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.js.DocumentLoader;


public class JSTest {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		SDB s=new SDB(new File("test.db"));//, Origin.uid);
		DocumentLoader l=new DocumentLoader(s);
		l.search("", null, new BuiltinFunc() {
			
			@Override
			public Object call(Context cx, Scriptable scope, Scriptable thisObj,
					Object[] args) {
				Scriptable s=(Scriptable)args[0];
				System.out.println(s.get("id", s)+" - "+ s.get("_body", s));
				return null;
			}
		});
		s.close();
	}

}
