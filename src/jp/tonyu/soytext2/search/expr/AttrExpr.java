package jp.tonyu.soytext2.search.expr;

import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.search.QueryResult;
import jp.tonyu.soytext2.search.SearchContext;


public class AttrExpr extends QueryExpression implements Comparable<AttrExpr> {
	final public String name;
	Object searchValue;
	public String getKey() {
		return name;
	}
	public Object getValue() {
		return searchValue;
	}
	AttrOperator op;
	@Override
	public int compareTo(AttrExpr other) {
		return name.compareTo(other.name);
	}
	public String name() {return name;}
	public AttrExpr(String name, Object value, AttrOperator op) {
		super();
		if (name.length()==0) Log.die("name is empty");
		this.name = name;
		this.searchValue = value;
		this.op = op;
	}
	@Override
	public QueryResult matches(DocumentScriptable d, SearchContext ctx) {
		Object actualValue=d.get(name);
		return new QueryResult(matches(actualValue));
	}
	public boolean matches(Object actualValue) {
		boolean res;
		if (actualValue==null) return false;
		if (op==AttrOperator.exact) {
			res= actualValue.equals(searchValue);
		} else if (op==AttrOperator.ge) {
			String srcStr=searchValue.toString();
			String actStr=actualValue.toString();
			// avalue > svalue     name:svalue
			res=actStr.indexOf(srcStr)>=0;
		} else {
			String srcStr=searchValue.toString();
			String actStr=actualValue.toString();
			// avalue<svalue   name:<svalue
			res=srcStr.indexOf(actStr)>=0;
		}
		return res;
	}
	@Override
	public String toString() {
		return name+AttrOperator.toString(op)+searchValue;
	}
}