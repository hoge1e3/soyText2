package jp.tonyu.soytext2.search.expr;

import jp.tonyu.soytext2.document.Document;
import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.search.QueryResult;
import jp.tonyu.soytext2.search.SearchContext;
import jp.tonyu.soytext2.search.TemplateMatchResult;


public class TemplateExpr extends QueryExpression implements Comparable<TemplateExpr> {
	public String name;
	public TemplateExpr(String attrName) {
		name=attrName;
	}
	@Override
	public QueryResult matches(DocumentScriptable d, SearchContext context) {
		TemplateMatchResult r=null;
		r=context.getTemplateMatchResult(name);
		Object actualValue=d.get(name);
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
