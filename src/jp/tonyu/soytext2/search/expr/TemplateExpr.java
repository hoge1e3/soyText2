package jp.tonyu.soytext2.search.expr;

import org.mozilla.javascript.ScriptableObject;

import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.search.QueryMatcher;
import jp.tonyu.soytext2.search.QueryResult;
import jp.tonyu.soytext2.search.TemplateMatchResult;


public class TemplateExpr extends QueryExpression implements Comparable<TemplateExpr> {
	public String name;
	public TemplateExpr(String attrName) {
		name=attrName;
	}
	@Override
	public QueryResult matches(DocumentScriptable d, QueryMatcher context) {
		TemplateMatchResult r=null;
		r=context.getTemplateMatchResult(name);
		Object actualValue=ScriptableObject.getProperty(d,name);
		if (actualValue!=null) {
			boolean fm = r.sendResult(actualValue);
			return new QueryResult(fm, true);
		}
		return new QueryResult(false, false);
	}
	public String getName() {
		return name;
	}
	@Override
	public String toString() {
		return name+":?";
	}
	@Override
	public int compareTo(TemplateExpr o) {
		return name.compareTo(o.name);
	}
}
