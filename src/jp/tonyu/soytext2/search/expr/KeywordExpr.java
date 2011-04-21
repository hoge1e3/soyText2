package jp.tonyu.soytext2.search.expr;

import jp.tonyu.soytext2.document.Document;
import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.search.QueryResult;
import jp.tonyu.soytext2.search.SearchContext;

public class KeywordExpr extends QueryExpression {
	String keyword;
	@Override
	public QueryResult matches(DocumentScriptable d, SearchContext context) {
		return new QueryResult( d.getDocument().content.toLowerCase().indexOf(keyword)>=0 );
	}
	public KeywordExpr(String keyword) {
		super();
		this.keyword = keyword.toLowerCase();
	}
	@Override
	public String toString() {
		return keyword;
	}
	public String getKeyword() {
		return keyword;
	}

}
