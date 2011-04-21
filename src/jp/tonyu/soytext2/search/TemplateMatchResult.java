package jp.tonyu.soytext2.search;

import jp.tonyu.soytext2.search.expr.AttrExpr;

public class TemplateMatchResult {
	AttrExpr cond;
	Object actualValue;
	public TemplateMatchResult(AttrExpr cond) {
		this.cond=cond;
	}
	public boolean sendResult(Object actualValue) {
		this.actualValue=actualValue;		
		return cond.matches(actualValue);
	}
	@Override
	public String toString() {
		return "(SearchCtx cond:"+cond+" actval:"+actualValue+")";// matched:"+matched+")";
	}
}