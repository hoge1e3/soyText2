package jp.tonyu.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.tonyu.parser.Parser;

public abstract class SPrintf {
	final StringBuffer buf=new StringBuffer();
	final String format;
	int pos=0;
	public SPrintf(String format, Object... args) {
		this.format=format;
		int i=0;
		while (true) {
			char c=format.charAt(pos);
			if (c=='%') {
				if (pos+1<format.length() && format.charAt(pos+1)=='%') {
					out("%");
				} else {
					onFormat(args[i]);
					i++;
				}
			} else {
				out(c);
			}
		}

	}
	public void out(Object str) {
		buf.append(str);
	}
	public abstract void onFormat(Object value);
	public boolean consume(String head) {
		boolean b = format.substring(pos).startsWith(head);
		if (b) {pos+=head.length();}
		return b;
	}
	public boolean consume(Pattern head) {
		boolean b = format.substring(pos).startsWith(head);
		if (b) {pos+=head.length();}
		return b;
	}
	public Matcher lastMatched() {
		
	}
}
