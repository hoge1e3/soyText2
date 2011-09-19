package jp.tonyu.js;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;

public class Args {
	static Pattern func=Pattern.compile("function\\s*\\w*\\(([^\\)]*)\\)");
	public static String[] getArgs(Function f) {
		boolean ent=(Context.getCurrentContext()==null);
		if (ent) Context.enter();
		String fs=""+f.getDefaultValue(String.class);
		if (ent) Context.exit();
		Matcher m=func.matcher(fs);
		if (m.find()) {
			String[] res=m.group(1).split(",");
			for (int i=0 ; i<res.length; i++) {
				res[i]=res[i].replaceAll("\\W", "");
			}
			return res;
		}
		//System.out.println(fs+" not found");
		return null;
	}
	public static void main(String[] args) {
		Object res=RunScript.eval("res=function (A, b, c) {}; ", new HashMap<String,Object>());
		String[] a=getArgs((Function)res);
		for (int i=0 ; i<a.length; i++) {
			System.out.println(a[i]);
		}
	}
}
