package soytext.parser;

import jp.tonyu.soytext2.js.Wrappable;



public class ParseError extends RuntimeException implements Wrappable {
	CharSequence source;
	int p; 
	public ParseError(CharSequence src, int p,String mesg) {
		super(mesg+" at "+p);
		this.source=src;
		this.p=p;
	}
	public CharSequence getSource() {
		return source;
	}
	public int getPos() {
		return p;
	}
}