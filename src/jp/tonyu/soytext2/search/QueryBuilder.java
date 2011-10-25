package jp.tonyu.soytext2.search;

import java.util.SortedSet;
import java.util.TreeSet;

import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.document.DocumentSet;
import jp.tonyu.soytext2.search.expr.AttrExpr;
import jp.tonyu.soytext2.search.expr.AttrOperator;

public class QueryBuilder {
	String cond;
//	DocumentSet documentSet;
	boolean emptyCond=false;
	SortedSet<AttrExpr> tmpls=new TreeSet<AttrExpr>();
	private QueryBuilder(String cond) {
		this.cond=cond;
		if (cond==null) {
			emptyCond=true;
			this.cond="";
		}
		//this.documentSet=documentSet;
	}
	public static QueryBuilder create(String cond) {
		return new QueryBuilder(cond);
	}
	public QueryBuilder tmpl(String name, Object value, AttrOperator op) {
		tmpls.add(new AttrExpr(name,value,op));
		if (emptyCond) {
			cond+=name+AttrOperator.toString(op)+"? ";
		}
		Log.d(this, "Cur Conds -"+cond+" tmpls - "+tmpls);
		return this;
	}
	public void addCond(String c) {
		cond+=c+" ";
	}
	public Query toQuery() {
		return new Query(cond, tmpls);
	}
}
