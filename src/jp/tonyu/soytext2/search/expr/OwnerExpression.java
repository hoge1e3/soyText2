package jp.tonyu.soytext2.search.expr;

import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.search.QueryMatcher;
import jp.tonyu.soytext2.search.QueryResult;

public class OwnerExpression extends QueryExpression {
	public final String owner;
	
	public OwnerExpression(String owner) {
		super();
		this.owner = owner;
	}

	@Override
	public QueryResult matches(DocumentScriptable d, QueryMatcher context) {
		// always true because index is used
		return new QueryResult(true);
	}

}
