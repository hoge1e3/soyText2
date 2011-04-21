package jp.tonyu.soytext2.search.expr;

public enum AttrOperator {
	exact,le,ge;
	public static AttrOperator fromString(String op) {
		if (op.equals(":=")) return exact;
		if (op.equals(":<")) return le;
		return ge;
	}

	public static String toString(AttrOperator op) {
		if (op.equals(exact)) return ":=";
		if (op.equals(le)) return ":<";
		return ":";
	}
} // :=   :<  :