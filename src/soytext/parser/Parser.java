package soytext.parser;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.tonyu.debug.Log;



public class Parser {
	public Parser(CharSequence src) {
		super();
		this.src = src;
	}
	CharSequence src;
	String indent="";
	public void dedent() { if (indent.length()>0) indent=indent.substring(1); }
	protected int p=0;
	Stack<Object> states=new Stack<Object>(); 
	public void reset() {
		p=0;
		states.clear();
	}
	public int position() {return p;}
	public int save() {return save("");}
	public int save(String debugInfo) {
		debug(debugInfo);indent+=" ";
		int prevSize=states.size();
		states.push(p);
		onSave(states);
		states.push(prevSize);
		return p;
	}
	public void onSave(Stack<Object> states) {}
	public void onRestore(Stack<Object> states) {}
	public boolean restore() {
		/*dedent();   
		debug("Fail");
		onRestore(states);
		this.p=(Integer)states.pop();
		return false;*/
		return restore(-1);
	}
	public boolean restore(int p) {
		dedent();   
		debug("Fail");
		int prevSize = (Integer)states.pop();
		onRestore(states);
		this.p=(Integer)states.pop();
		if (p>=0) this.p=p;
		if (prevSize!=states.size()) Log.die(" Stack size not match "+prevSize+"!="+states.size());
		return false;
	}
	public boolean fail() {
		return restore();
	}
	public boolean ok() {
		dedent();
		debug("OK"); 
		int prevSize = (Integer)states.pop();
		while (states.size()>prevSize) states.pop();
		return true;
	}
	public boolean read(String str) {
		skipSpace();
		return readWithoutSpace(str);
	}
	public boolean readWithoutSpace(String str) {
		CharSequence c=current();
		for (int i=0; i<str.length(); i++) {
			if (i>=c.length() || c.charAt(i)!=str.charAt(i)) return false;
		}
		p+=str.length();
		//refreshMax();
		return true;
	}
	
	public ParseError getLastError() {
		return lastError;
	}
	public void setLastError(ParseError e) {
		lastError=e;
	}
	Matcher lastMatcher;
	public boolean read(Pattern pattern) {
		return matcher(pattern).lookingAt();
	}
	public boolean readWithoutSpace(Pattern pattern) {
		return matcherWithoutSpace(pattern).lookingAt();
	}
	public String group() {
		return lastMatcher.group();
	}
	public String group(int group) {
		return lastMatcher.group(group);
	}
	public Matcher matcher(Pattern pattern) {
		skipSpace();
		return matcherWithoutSpace(pattern);
	}
	ParseError lastError;
	public Matcher matcherWithoutSpace(Pattern pattern) {
		CharSequence current = current();
		//StringBuffer b=new StringBuffer(current);
		lastMatcher=pattern.matcher(current);
		//Debug.syslog("Looking "+b);
		if (lastMatcher.lookingAt()) {
			p+=lastMatcher.end();
		} else {
			error(pattern+" expected '"+current+"'");
		}
		return lastMatcher;
	}
	public void error(String mesg) {
		if (lastError==null || lastError.p<p) {
			lastError=new ParseError(src, p, mesg);
		}
	}
	public void debug(String elem) {
		//Debug.syslog(indent+"Reading "+elem+" at "+p+" "+current());
	}
	public boolean endOfSource() {
		int s=p;
		skipSpace();
		boolean res=p>=src.length();
		p=s;
		return res; 
	}
	Pattern spacePattern=Pattern.compile("[ \\n\\t\\r]*");
	public void setSpacePattern(Pattern p) {
		spacePattern=p;
	}
	public void skipSpace() {
		if (spacePattern==null) return;
		matcherWithoutSpace(spacePattern);
	}
	public CharSequence current() {
		final int p=this.p;
		return new CharSequence() {
			@Override
			public CharSequence subSequence(int b, int e) {
				return src.subSequence(p+b, p+e);
			}
			
			@Override
			public int length() {
				return src.length()-p;
			}
			
			@Override
			public char charAt(int i) {
				return src.charAt(i+p);
			}
			
			@Override
			public String toString() {
				StringBuilder s=new StringBuilder();
				for (int i=p-5; i<=p+5; i++) {
					if (i==p) s.append("^");
					if (i>=0 && i<src.length()) {
						
						char charAt = src.charAt(i);
						s.append(charAt!='\n' ? charAt : "\\n");
					}
				}
				return s.toString();
			}

		};
	}
	public static String[] Split(String regex, String string) {
		Pattern p=Pattern.compile(regex);
		return p.split(string);
	}
	public static void main(String[] args) {
		//Parser p=new Parser(" a");
		Matcher m = Pattern.compile("^[ \\n\\t]*").matcher(" a");
		System.out.println(m.find());
		
	}
	public static String join(String sep, Iterable<? extends Object> array) {
		String ss="";
		StringBuilder b=new StringBuilder();
		for (Object e: array) {
			b.append(ss+e);
			ss=sep;
		}
		return b.toString();
		
	}
}
