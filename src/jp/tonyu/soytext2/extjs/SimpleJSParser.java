package jp.tonyu.soytext2.extjs;

import java.util.regex.Pattern;

import jp.tonyu.debug.Log;
import jp.tonyu.js.Wrappable;
import jp.tonyu.parser.Parser;

public class SimpleJSParser extends Parser  {
	public SimpleJSParser(CharSequence src) {
		super(src);
	}
	private static final Pattern NUMPAT = Pattern.compile("-?\\d+(\\.\\d+)?");
	private static final Pattern SYM = Pattern.compile("[\\$\\w][\\$\\w\\d]*");
	private static final Pattern FIELDSYM = Pattern.compile("@([\\$\\w][\\$\\w\\d]*)");
	private static final Pattern ONELINECOMMENT = Pattern.compile("//.*\\n");
	//               /\*/?([^/]|[^*]/)*\*/  -> /\*/?([^/]*([^*]/)*)*\*/
	private static final Pattern MULTILINECOMMENT = Pattern.compile("/\\*/?([^/]*([^*]/)*)*\\*/");
	private static final Pattern PUNCT1 = Pattern.compile("(\\.)|(\\{)|(\\})|(\\()|(\\))|(\\[)|(\\])|(\\;)|(\\,)|(\\<)|(\\>)|(\\+)|(\\-)|(\\*)|(\\%)|(\\&)|(\\|)|(\\^)|(\\!)|(\\~)|(\\?)|(\\:)|(\\=)|(\\/)");
	private static final Pattern PUNCT2 = Pattern.compile("(\\<\\=)|(\\>\\=)|(\\=\\=)|(\\!\\=)|(\\+\\+)|(\\-\\-)|(\\<\\<)|(\\>\\>)|(\\&\\&)|(\\|\\|)|(\\+\\=)|(\\-\\=)|(\\*\\=)|(\\%\\=)|(\\&\\=)|(\\|\\=)|(\\^\\=)");
	private static final Pattern PUNCT3 = Pattern.compile("(\\=\\=\\=)|(\\!\\=\\=)|(\\>\\>\\>)|(\\<\\<\\=)|(\\>\\>\\=)");
	private static final Pattern NORMCHAR = Pattern.compile("[^\\\\\"]+"); // [^\\"]+
	private static final Pattern NORMREGCHAR = Pattern.compile("[^\\\\/]+"); // [^\\/]+
	private static final Pattern ESCCHAR = Pattern.compile("\\\\(.)"); // \\(.) 
	@Override
	public void skipSpace() {
		super.skipSpace();
		String group = group();
		/*int l = group.length();
		if (l>0) {
			Log.d(this, "len="+l);
			for (int i=0 ;i<l; i++) {
				Log.d(this, (int)group.charAt(i));
			}
		}*/
		out(group);
	}
	public final StringBuffer buf=new StringBuffer();
	private void out(String string) {
		buf.append(string);		
	}
	public void parse() {
		int loop=0;
		while (!endOfSource()) {
			int ps=p;
			if (!parse1()) break;
			if (ps==p || loop>1000) Log.die( "Loop! " +current());
			loop++;
		}
	}
	public boolean parse1() {
		if (read(NUMPAT) || read(ONELINECOMMENT) ||  read(MULTILINECOMMENT) ||read(SYM) || read(PUNCT3) || read(PUNCT2) || read(PUNCT1)) {
			out(group());
			return true;
		} else if (read("\\")) {
			out(" function ");
			return true;
		} else if (str() || regex()) {
			return true;
		} else if (read(FIELDSYM)) {
			out("this."+group(1));
			return true;
		} else {
			Log.d("Cannot read", current());
			out("/*Cannot read - "+current()+"*/");
			return false;
		}
	}
	private boolean regex() {
		save("REGEX");
		if (!read("~/")) return fail();
		out("/");
		StringBuilder buf = new StringBuilder();
		while (true) {
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

	private boolean str() {
		save("STR");
		if (!read("\"")) return fail();
		out("\"");
		StringBuilder buf = new StringBuilder();
		while (true) {
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
	public static void main(String[] args) {
		String jssrc="\\a(x,y) \n{@aho=@aho+\"@baka\"\n}";
		SimpleJSParser s = new SimpleJSParser(jssrc);
		s.parse();
		System.out.println(s.buf);
	}
}
