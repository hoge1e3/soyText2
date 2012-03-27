package jp.tonyu.soytext2.search.expr;

import org.mozilla.javascript.Function;

import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.search.QueryMatcher;
import jp.tonyu.soytext2.search.QueryResult;

public class InstanceofExpr extends QueryExpression {
	public final String klass;

	public InstanceofExpr(String klass) {
		super();
		this.klass = klass;
	}

	@Override
	public QueryResult matches(DocumentScriptable d, QueryMatcher context) {
		// always true because index is used
		return new QueryResult(true);
	}
	@Override
	public String toString() {
		return "(is "+klass+")";
	}

}
