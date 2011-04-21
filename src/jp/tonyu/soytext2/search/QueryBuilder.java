package jp.tonyu.soytext2.search;

import java.util.SortedSet;
import java.util.TreeSet;

import jp.tonyu.soytext2.document.DocumentSet;
import jp.tonyu.soytext2.search.expr.AttrExpr;
import jp.tonyu.soytext2.search.expr.AttrOperator;

public class QueryBuilder {
	String cond;
//	DocumentSet documentSet;
	SortedSet<AttrExpr> tmpls=new TreeSet<AttrExpr>();
	private QueryBuilder(String cond) {
		this.cond=cond;
		//this.documentSet=documentSet;
	}
	public static QueryBuilder create(String cond) {
		return new QueryBuilder(cond);
	}
	public QueryBuilder tmpl(String name, Object value, AttrOperator op) {
		tmpls.add(new AttrExpr(name,value,op));
		return this;
	}
	public Query toQuery() {
		return new Query(cond, tmpls);
	}
}