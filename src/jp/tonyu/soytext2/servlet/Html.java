package jp.tonyu.soytext2.servlet;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.tonyu.parser.Parser;


public class Html {
	static Pattern raw=Pattern.compile("[^%]*");
	static Pattern tmplPat=Pattern.compile("%.");
	public static String p(String tmpl, String... params) {
		StringBuilder res=new StringBuilder();
		Parser p=new Parser(tmpl);
		p.setSpacePattern(null);
		int i=0;
		while (true) {
			Matcher m=p.matcher(raw);
			res.append( m.group() );
			m=p.matcher(tmplPat);
			if (m.lookingAt()) {
				if (m.group().equals("%a") && i<params.length) {
					res.append("\""+HTMLDecoder_encode(params[i])+"\"");
				} else if (m.group().equals("%s") && i<params.length) {
					res.append(params[i]);
				} else if (m.group().equals("%t") && i<params.length) {
					res.append(HTMLDecoder_encode(params[i]));
				} else if (m.group().equals("%u")  && i<params.length){
					try {
						res.append(URLEncoder.encode(params[i],"utf-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				} else {
					res.append(m.group());
				}
				i++;
			} else break;
		}
		return res.toString();
	}
	private static String HTMLDecoder_encode(String string) {
		if (string==null) return "null";
		return HTMLDecoder.encode(string);
	}
	public static void main(String[] args) {
		System.out.println(Html.p("test %a desu", "a"));
	}
}
