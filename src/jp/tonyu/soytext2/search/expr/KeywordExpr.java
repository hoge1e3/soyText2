package jp.tonyu.soytext2.search.expr;

import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.search.QueryMatcher;
import jp.tonyu.soytext2.search.QueryResult;

public class KeywordExpr extends QueryExpression {
	String keyword;
	@Override
	public QueryResult matches(DocumentScriptable d, QueryMatcher context) {
		if (d==null) Log.die("Query d is null");
		if (d.getDocument()==null) Log.die(d+" getDocument is null");
		if (d.getDocument().content==null) Log.die(d+"/"+ d.getDocument()+"  getDocument.content is null");
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
