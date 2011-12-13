package jp.tonyu.soytext2.extjs;

import java.util.HashSet;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Pattern;

import org.mozilla.javascript.Scriptable;

import jp.tonyu.debug.Log;
import jp.tonyu.js.BlankScriptableObject;
import jp.tonyu.js.Wrappable;
import jp.tonyu.parser.ParseError;
import jp.tonyu.parser.Parser;
import jp.tonyu.soytext2.js.CompileResult;
import jp.tonyu.soytext2.js.DefaultCompiler;
import jp.tonyu.soytext2.js.DocumentLoader;
import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.js.DocumentSourceable;
import jp.tonyu.soytext2.js.JSSession;
import jp.tonyu.soytext2.servlet.HttpContext;



public class ClassConverter extends Parser implements Wrappable, CompileResult {
	private static final Pattern NUMPAT = Pattern.compile("-?\\d+(\\.\\d+)?");
	private static final Pattern SYM = Pattern.compile("[\\$\\w][\\$\\w\\d]*");
	private static final Pattern ONELINECOMMENT = Pattern.compile("//.*\\n");
	//               /\*/?([^/]|[^*]/)*\*/  -> /\*/?([^/]*([^*]/)*)*\*/
	private static final Pattern MULTILINECOMMENT = Pattern.compile("/\\*/?([^/]*([^*]/)*)*\\*/");
	private static final Pattern PUNCTNOCURLDOT1 = Pattern.compile("(\\()|(\\))|(\\[)|(\\])|(\\;)|(\\,)|(\\<)|(\\>)|(\\+)|(\\-)|(\\*)|(\\%)|(\\&)|(\\|)|(\\^)|(\\!)|(\\~)|(\\?)|(\\:)|(\\=)|(\\/)");
	private static final Pattern PUNCTNOCURLDOT2 = Pattern.compile("(\\<\\=)|(\\>\\=)|(\\=\\=)|(\\!\\=)|(\\+\\+)|(\\-\\-)|(\\<\\<)|(\\>\\>)|(\\&\\&)|(\\|\\|)|(\\+\\=)|(\\-\\=)|(\\*\\=)|(\\%\\=)|(\\&\\=)|(\\|\\=)|(\\^\\=)");
	private static final Pattern PUNCTNOCURLDOT3 = Pattern.compile("(\\=\\=\\=)|(\\!\\=\\=)|(\\>\\>\\>)|(\\<\\<\\=)|(\\>\\>\\=)");
	private static final Pattern NORMCHAR = Pattern.compile("[^\\\\\"]+"); // [^\\"]+
	private static final Pattern NORMREGCHAR = Pattern.compile("[^\\\\/]+"); // [^\\/]+
	private static final Pattern ESCCHAR = Pattern.compile("\\\\(.)"); // \\(.) 
	private static final String THIS = "_THIS_";
	int pass=0;
	boolean success=false;
	DocumentScriptable src;
	String lastClassName;
	private long compiledTime;
	@Override
	public DocumentScriptable getDocumentSource() {
		return src;
	}
	public ClassConverter(DocumentScriptable src) {
		super(src.get(HttpContext.ATTR_BODY)+"");
		this.src=src;
		compiledTime=getDocumentSource().getDocument().lastUpdate;
		Object object = src.get(DefaultCompiler.ATTR_SCOPE);
		if (object instanceof Scriptable) {map=(Scriptable)object;}
		else {map=new BlankScriptableObject();}
	}
	@Override
	public boolean isUp2Date() {
		return getDocumentSource().getDocument().lastUpdate==compiledTime;
	}
	@Override
	public void onSave(Stack<Object> states) {
		states.push(buf.length());
	}
	@Override
	public void onRestore(Stack<Object> states) {
		int l=(Integer)states.pop(); 
		//Debug.syslog("Restore buffer into "+l+" len="+buf.length());
		buf.delete(l, buf.length());
	}
	ClassDef classDef;
	private String lastSymbol;
	final Scriptable map; // scope
	int loopCnt=0;
	public boolean loopCheck() {
		loopCnt++;
		if (loopCnt>10000) Log.die("Infinite loop?");
		return true;
	}
	public synchronized String generateCode() {
		if (pass==0) parseInterface();
		if (pass==1) {
			pass=2;
			success=false;
			reset();
			classDecl();
			success=true;
		}
		return buf.toString();
	}
	public synchronized boolean parseInterface() {
		if (pass>=1) return success;
		pass=1;
		success=false;
		reset();
		classDecl();
		if (!endOfSource()) {
			if (getLastError()==null) {
				error("Program remains but definition read completely");
			}
			return false;
		}
		success=true;
		return true;
	}
	@Override
	public void reset() {
		super.reset();
		buf.delete(0, buf.length());
			
	}
	public String getBuffer() {
		return buf.toString();
	}
	public boolean classDecl() {
		boolean tonyuStyle=false;
		// normal      ::  class ClassName { decl*   }
		// tonyuStyle  ::  decl* 
		save("ClassDecl");
		if (!read("class")) {
			tonyuStyle=true;
			lastClassName=getClassName(src);
			
			//return fail();
		}else {
			if (!className()) return fail();
		}
		out(lastClassName+"=Class.create(");
		ClassDef superClass=null;
		/*if (read("extends")) {
			if (!className()) return fail();
			out(lastClassName+",");
			IDocumentRef sd=map.map.get(lastClassName);
			if (sd==null) Debug.die("Class "+lastClassName+" not found");
			superClass = getClassDef(sd);
		} else {*/
			Object v=src.get("extends");
			if (v instanceof DocumentScriptable) {
				DocumentScriptable sd = (DocumentScriptable) v;
				superClass=getClassDef(sd);
				Log.d(this,"SuperClass !"+superClass);
				if (superClass==null) return fail();
				out(getClassName(sd)+",");
			}
		//}
		if (pass==1) {
			classDef=new ClassDef(src,superClass);
			Log.d(this,lastClassName+"::ClassDef - "+classDef);
		}
		//if (pass==2)
		out("{");
		if ((!tonyuStyle) && !read("{")) return fail();
		while (loopCheck()) {
			if (!decl()) break;
		}
		if ((!tonyuStyle) && !read("}")) return fail();
		out("__dummy:false});");
		return ok();
	}
	private static String getClassName(DocumentScriptable src) {
		return (""+src.get("name")).replaceAll("\\..*$", "");
	}
	private ClassDef getClassDef(DocumentScriptable sd) {
		ClassDef res;
		CompileResult compileRes =  DocumentLoader.curJsSesssion().compile(sd);
		if (compileRes==null) Log.die("Compile Result of "+sd+" null. Perhaps forget compiler:extJS?");
		ClassConverter c= Log.notNull(
				(ClassConverter)compileRes,
				"Compile Result of "+sd+" null. Perhaps forget compiler:extJS?");
		if (!c.parseInterface()) {
			Log.d(this, "Parse Error "+c.getLastError());
			setLastError(new ParseError(sd.get(HttpContext.ATTR_BODY)+"", 0,"Error in superclass : "+c.getLastError()));
		}
		res=c.classDef;
		return res;
	}
	private boolean decl() {
		return varDecl() || funcDecl() || comment();
	}
	Vector<String> args;
	private boolean funcDecl() {
		save("funcDecl");
		if (!read("function")) return fail();
		if (!symbol()) return fail();
		String name=lastSymbol;
		classDef.methods.add(name);
		out(" "+name+":function ( ");
		if (!read("(")) return fail();
		args=new Vector<String>();
		if (symbol()) {
			args.add(lastSymbol);
			out(lastSymbol+" ");
			while (loopCheck()) {
				if (!read(",")) break;
				if (!symbol()) break;
				out(","+lastSymbol+" ");
				args.add(lastSymbol);
			}			
		}
		if (!read(")")) return fail();
		out(")");
		if (!block(true)) return fail();	
		out(",");
		return ok();
	}
	private boolean block(boolean topMethod) {
		save("block");
		if (!read("{")) return fail();
		out("{");
		if (topMethod) {
			out("var "+THIS+"=this;");
		}
		while (loopCheck()) {
			if (nonblock()) {
				
			} else if (block(false)) {
				
			} else break;
		}
		if (!read("}")) return fail();
		out("}");
		return ok();
	}

	private boolean varDecl() {
		save("varDecl");
		if (!read("var")) return fail();
		if (!symbol()) return fail();
		classDef.fields.add(lastSymbol);
		while (loopCheck()) {
			if (!read(",")) break;
			if (!symbol()) break;
			classDef.fields.add(lastSymbol);
		}
		if (!read(";")) return fail();
		return ok();
	}
	
	private boolean symbol() {
		save("sym");
		if (!read(SYM)) return fail();
		lastSymbol=group();
		return ok();
	}
	private boolean className() {
		save("className");
		if (!symbol()) return fail();
		lastClassName=lastSymbol;
		return ok();
	}

	StringBuffer buf=new StringBuffer();
	private void out(String string) {
		buf.append(string);		
	}
	@Override
	public void skipSpace() {
		super.skipSpace();
		out(group());
	}

	
	/*
	 * http://www2u.biglobe.ne.jp/~oz-07ams/prog/ecma262r3/7_Lexical_Conventions.html#section-7.1
	 * 7 字句について (Lexical Convention)
7.1 Unicode 制御文字 (Unicode Format-Control Characters)
7.2 空白 (White Space)
7.3 行終端子 (Line Terminators)
7.4 コメント (Comments)
7.5 トークン (Tokens)
 7.6 識別子 (Identifiers)
 7.7 区切り子 (Punctuators)
 7.8 リテラル (Literals)
 7.8.1 Null リテラル (Null Literals)
 7.8.2 Boolean リテラル (Boolean Literals)
 7.8.3 数値リテラル (Numeric Literals)
 7.8.4 文字列リテラル (String Literals)
 7.8.5 正規表現リテラル (Regular Expression Literals)
	 */
	private boolean nonblock() {
		return comment() || regex() || callHead() || callTail() || punctNoCurlDot() || num() || str() ;
	}
	private boolean callTail() {
		save("CALLTAIL");
		if (!read(".")) return fail();
		if (!symbol()) return fail();
		out(".");out(lastSymbol);
		return ok();
	}
	private boolean callHead() {
		save("CALLHEAD");
		if (!symbol()) return fail();
		if (classDef.isField(lastSymbol)) {
			out("("+THIS+"."+lastSymbol+")");
		} else if (classDef.isMethod(lastSymbol)) {
			out("("+THIS+"."+lastSymbol+".bind("+THIS+"))");
		} else out(lastSymbol+" "); 
		return ok();
	}
	private boolean comment() {
		return oneLineComment() || multiLineComment();
	}
	private boolean oneLineComment() {
		if (read(ONELINECOMMENT)) {
			out("\n");
			return true;
		}
		return false;
	}
	private boolean multiLineComment() {
		
		if (read(MULTILINECOMMENT)) {
			return true;
		}
		return false;
	}
	private boolean punctNoCurlDot() {
		//( ) [ ] . ; , < > <= >= == != === !== + - * % ++ -- << >> >>> & | ^ ! ~ && || ? : = += -= *= %= <<= >>= >>>= &= |= ^="
		save("PunctNoCurl");
		if (read(PUNCTNOCURLDOT3) || read(PUNCTNOCURLDOT2) || read(PUNCTNOCURLDOT1)) {
			out(group()+" ");
			//if (";".equals(group())) out("\n");
			return ok();
		}
		return fail();
	}
	private boolean str() {
		save("STR");
		if (!read("\"")) return fail();
		out("\"");
		StringBuilder buf = new StringBuilder();
		while (loopCheck()) {
			if (readWithoutSpace(NORMCHAR)) {
				buf.append(group());
				out(group());
			} else if (readWithoutSpace(ESCCHAR)) {
				buf.append(group(1));
				out(group());
			} else break;
		}
		if (!read("\"")) return fail();
		out("\" ");
		return ok();
	}
	private boolean num() {
		save("NUM");
		if (!read(NUMPAT)) return fail();
		out(group()+" ");
		return ok();
	}
	private boolean regex() {
		save("REGEX");
		if (!read("~/")) return fail();
		out("/");
		StringBuilder buf = new StringBuilder();
		while (loopCheck()) {
			if (readWithoutSpace(NORMREGCHAR)) {
				buf.append(group());
				out(group());
			} else if (readWithoutSpace(ESCCHAR)) {
				buf.append(group(1));
				out(group());
			} else break;
		}
		if (!read("/")) return fail();
		String opt="";
		while (true) {
			if (read("g")) opt+="g";
			else if (read("i")) opt+="i";
			else if (read("m")) opt+="m";
			else break;
		}
		out("/"+opt+" ");
		return ok();
	}
	/*public static void main(String[] args) {
		String buf="+= -= >> << + -";
		ClassConverter c=new ClassConverter(buf);
		System.out.println(c.punctNoCurlDot());
		c.debug("A");
		System.out.println(c.punctNoCurlDot());
		c.debug("B");
		System.out.println(c.punctNoCurlDot());
		c.debug("C");
		System.out.println(c.punctNoCurlDot());
		c.debug("D");
		System.out.println(c.punctNoCurlDot());
		c.debug("E");
		System.out.println(c.punctNoCurlDot());
		c.debug("F");
		
	}*/
}
