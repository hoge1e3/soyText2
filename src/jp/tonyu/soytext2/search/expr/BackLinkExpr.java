package jp.tonyu.soytext2.search.expr;

import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.search.QueryMatcher;
import jp.tonyu.soytext2.search.QueryResult;

public class BackLinkExpr extends QueryExpression {
	public final String toId;
	
	public BackLinkExpr(String toId) {
		super();
		this.toId = toId;
	}

	@Override
	public QueryResult matches(DocumentScriptable d, QueryMatcher context) {
		// always true because index is used
		return new QueryResult(true);
	}

}
