package jp.tonyu.soytext2.search.expr;

import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.search.QueryResult;
import jp.tonyu.soytext2.search.SearchContext;


public abstract class QueryExpression {
	public String source;
	public static QueryExpression parse(String src) {
		return null;
	}
	public abstract QueryResult matches(DocumentScriptable d, SearchContext context);
}
