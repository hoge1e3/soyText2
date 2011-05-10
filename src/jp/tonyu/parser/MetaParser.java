package jp.tonyu.parser;

public class MetaParser extends Parser {
	public MetaParser(CharSequence src) {
		super(src);
		// TODO Auto-generated constructor stub
	}

	public boolean decls() {
		decl();
		if (!endOfSource()) {
			error("Not eos");
			return false;
		}
		return true;
	}
	
	private void decl() {
		// TODO Auto-generated method stub
		
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

}
