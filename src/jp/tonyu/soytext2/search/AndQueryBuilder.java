package jp.tonyu.soytext2.search;

import java.util.SortedSet;
import java.util.TreeSet;

import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.document.DocumentSet;
import jp.tonyu.soytext2.search.expr.AndExpr;
import jp.tonyu.soytext2.search.expr.AttrExpr;
import jp.tonyu.soytext2.search.expr.AttrOperator;
import jp.tonyu.soytext2.search.expr.InstanceofExpr;
import jp.tonyu.soytext2.search.expr.KeywordExpr;
import jp.tonyu.soytext2.search.expr.QueryExpression;

public class AndQueryBuilder {
	AndExpr cond=new AndExpr();
	
	public AndQueryBuilder instof(String klassId) {
		cond.add(new InstanceofExpr(klassId));
		return this;
	}
	public AndQueryBuilder attr(String name, Object value, AttrOperator op) {
		cond.add(new AttrExpr(name,value,op));
		return this;
	}
	public AndQueryBuilder keyword(String keyword) {
		cond.add(new KeywordExpr(keyword));
		return this;
	}

	public Query toQuery() {
		return new Query(new QueryTemplate(cond));
	}
}
