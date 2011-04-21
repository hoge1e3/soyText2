package jp.tonyu.soytext2.search.expr;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.search.QueryResult;
import jp.tonyu.soytext2.search.SearchContext;



public class AndExpr extends QueryExpression implements Iterable<QueryExpression> {
	List<QueryExpression> conditions=new Vector<QueryExpression>();
	@Override
	public Iterator<QueryExpression> iterator() {
		return conditions.iterator();
	}
	@Override
	public QueryResult matches(DocumentScriptable d, SearchContext ctx) {
		boolean fm=true, tm=true;
		//boolean debugsw=toString() .indexOf("aiu")>=0;//SearchLog".equals( d.str("type") );
		for (QueryExpression c:conditions) {
			QueryResult r=c.matches(d, ctx);
			fm &= r.filterMatched;
			tm &= r.templateMatched;
			//if (debugsw) {
			//}
			if (fm==false && tm==false) break;
		}
		return new QueryResult(fm,tm); 
	}
	public void add(QueryExpression cond) {
		conditions.add(cond);
	}
	@Override
	public String toString() {
		StringBuilder b=new StringBuilder();
		for (QueryExpression c:conditions) {
			b.append(c+" ");
		}
		return b.toString();
	}
}
