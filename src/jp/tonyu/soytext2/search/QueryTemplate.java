package jp.tonyu.soytext2.search;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import jp.tonyu.soytext2.search.expr.AndExpr;
import jp.tonyu.soytext2.search.expr.AttrExpr;
import jp.tonyu.soytext2.search.expr.QueryExpression;
import jp.tonyu.soytext2.search.expr.TemplateExpr;

public class QueryTemplate {
	QueryExpression cond;
	SortedSet<TemplateExpr> templates=new TreeSet<TemplateExpr>();
	public QueryTemplate(QueryExpression cond, SortedSet<TemplateExpr> templates) {
		super();
		this.cond = cond;
		this.templates = templates;
	}
	public QueryTemplate(QueryExpression expr) {
		this(expr, new TreeSet<TemplateExpr>());
	}
	public QueryExpression getCond() {
		return cond;
	}
	public SortedSet<TemplateExpr> getTemplates() {
		return templates;
	}
	public boolean contains(String name) {
		for (TemplateExpr c:templates) {
			if (c.name.equals(name)) return true;
		}
		return false;
	}
	@Override
	public String toString() {
		return "(QT cond:("+cond+") tmpl:("+templates+"))";
	}
	public Query apply(SortedSet<AttrExpr> attrs) {
		return new Query(this ,attrs);
	}
	
}
