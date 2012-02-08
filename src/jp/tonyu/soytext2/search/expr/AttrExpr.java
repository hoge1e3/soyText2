package jp.tonyu.soytext2.search.expr;

import org.mozilla.javascript.ScriptableObject;

import jp.tonyu.debug.Log;
import jp.tonyu.js.Scriptables;
import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.search.QueryResult;
import jp.tonyu.soytext2.search.QueryMatcher;


public class AttrExpr extends QueryExpression implements Comparable<AttrExpr> {
	final public String name;
	final Object searchValue;
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
		if(value==null) Log.d(this,"searchValue is null");
		this.name = name;
		this.searchValue = value;
		this.op = op;
	}
	@Override
	public QueryResult matches(DocumentScriptable d, QueryMatcher ctx) {
		Object actualValue=ScriptableObject.getProperty(d,name);
		return new QueryResult(matches(actualValue));
	}
	public boolean matches(Object actualValue) {
		boolean res;
		if (actualValue==null) return false;
		if (op==AttrOperator.exact) {
			res= actualValue.equals(searchValue);
		} else if (op==AttrOperator.ge) {
			String srcStr=searchValue+"";
			String actStr=actualValue.toString();
			// avalue > svalue     name:svalue
			res=actStr.indexOf(srcStr)>=0;
		} else {
			String srcStr=searchValue+"";
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
