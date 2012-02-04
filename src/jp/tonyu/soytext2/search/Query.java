package jp.tonyu.soytext2.search;

import java.util.SortedSet;
import java.util.TreeSet;

import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.search.expr.AttrExpr;


public class Query {
	QueryTemplate queryTemplate;
	SortedSet<AttrExpr> attrs;
	public QueryTemplate getExpr() {
		return queryTemplate;
	}
	@Override
	public String toString() {
		return "(Query "+queryTemplate+" attrs:"+attrs+")";
	}

	public QueryTemplate getFilterTemplate() {
		return queryTemplate;
	}

	public SortedSet<AttrExpr> getAttrs() {
		return attrs;
	}
	private Query(String expr) {
		QueryTemplateParser p=new QueryTemplateParser(expr);
		this.queryTemplate=p.parse();	
		attrs=new TreeSet<AttrExpr>();
	}

	public Query(QueryTemplate queryTemplate, SortedSet<AttrExpr> attrs) {
		super();
		Log.d("Query", "Create query cond="+queryTemplate+" attrs="+attrs);
		this.queryTemplate = queryTemplate;
		this.attrs = attrs;
	}

	public Query(String cstr, SortedSet<AttrExpr> templateValues) {
		this(new QueryTemplateParser(cstr).parse(),templateValues);
	}
	public QueryResult matches(DocumentScriptable d) {
		QueryMatcher s = new QueryMatcher(d,this);
		return s.matches();
	}
	/*private Filter(FilterTemplate qt) {
		this.queryTemplate=qt;	
	}*/
	public static Query create(String cond) {
		return new Query(cond);
	}
	/*public Query tmpl(String name, Value value, AttrOperator op) {
		attrs.add(new AttrExpr(name,value,op));
		return this;
	}*/
}
