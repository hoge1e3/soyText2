package jp.tonyu.soytext2.search.expr;

import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.search.QueryResult;
import jp.tonyu.soytext2.search.SearchContext;

public class NotExpr extends QueryExpression {
	QueryExpression cond;
	@Override
	public QueryResult matches(DocumentScriptable d, SearchContext context) {
		QueryResult res=cond.matches(d, context);		
		return new QueryResult(!res.filterMatched);
	}
	public NotExpr(QueryExpression cond) {
		super();
		this.cond = cond;
	}

}
