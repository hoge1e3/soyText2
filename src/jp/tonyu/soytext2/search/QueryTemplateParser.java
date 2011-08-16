package jp.tonyu.soytext2.search;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.tonyu.debug.Log;
import jp.tonyu.parser.Parser;
import jp.tonyu.soytext2.search.expr.AndExpr;
import jp.tonyu.soytext2.search.expr.AttrExpr;
import jp.tonyu.soytext2.search.expr.AttrOperator;
import jp.tonyu.soytext2.search.expr.KeywordExpr;
import jp.tonyu.soytext2.search.expr.NotExpr;
import jp.tonyu.soytext2.search.expr.QueryExpression;
import jp.tonyu.soytext2.search.expr.TemplateExpr;

public class QueryTemplateParser {
	Parser p;
	SortedSet<TemplateExpr> templates=new TreeSet<TemplateExpr>();
	public QueryTemplateParser(CharSequence s) {
		p=new Parser(s);
	}
	public QueryTemplate parse() {
		if (parseCond() && p.endOfSource()) {
			return new QueryTemplate(lastCond, templates);
		}
		throw p.getLastError();
	}
	public boolean parseCond() {
		// query := condition
		// condition:= andcond
		return parseAndCond();
	}
	QueryExpression lastCond;
	public boolean parseAndCond() {
		// andcond := (singlecond)*
		AndExpr a=new AndExpr();
		while (parseNotSingleCond()) {
			a.add(lastCond);
			Log.d(this,"Adding cond = "+lastCond.getClass()+" : "+lastCond);
		}
		lastCond=a;
		return true;
	}
	static final Pattern notPat=Pattern.compile("-");
	//  notsinglecond := -?  singlecond 
	public boolean parseNotSingleCond() {
		boolean negate=false;
		if (p.read(notPat)) {
			negate=true;
		}
		if (!parseSingleCond()) return false;
		if (negate) {
			if (lastCond instanceof TemplateExpr) {
				Log.die("-"+lastCond+" not allowed");
			}
			lastCond=new NotExpr(lastCond);
		}
		return true;		
	}
	public boolean parseSingleCond() {
		// singlecond := templateCond | attrcond | keywordcond  
		return parseTemplateCond() || parseAttrCond() || parseKeywordCond();
	}
	static final Pattern keywordCondPat=Pattern.compile("[^\\s]+");
	private boolean parseKeywordCond() {
		// keywordcond := [^\s]+
		// TODO Auto-generated method stub
		Matcher m=p.matcher(keywordCondPat);
		if (m.lookingAt()) {
			//Debug.syslog("PArsed!! ("+m.group()+")");
			//if (m.group().indexOf(" ")>=0) Debug.die(m.group()+";Whey space contain?");
			lastCond=new KeywordExpr( m.group() );
			return true;
		}
		return false;
	}
	static final Pattern tmplCondPat=Pattern.compile("([^:\\s]+):[=<]?\\?");
	public boolean parseTemplateCond() {
		// tmplcond := ([^:]+):?
		Matcher m=p.matcher(tmplCondPat);
		if (m.lookingAt()) {
			String attrName=m.group(1);
			lastCond=new TemplateExpr(attrName);
			templates.add((TemplateExpr)lastCond);
			return true;
		}
		return false;
	}
	static final Pattern attrCondPat=Pattern.compile("([^:\\s]+)(:[=<]?)([^\\s]+)");
	public boolean parseAttrCond() {
		// attrcond := [^\:]+:[=<]?[^\s]+
		Matcher m=p.matcher(attrCondPat);
		if (m.lookingAt()) {
			String attrName=m.group(1);
			String compType=m.group(2);
			String attrValue=m.group(3);
			lastCond=new AttrExpr(attrName, attrValue, AttrOperator.fromString(compType));
			return true;
		}
		return false;
	}
	public static void main(String[] args) {
		System.out.println( new QueryTemplateParser("type:=SavedSearch condition:?").parse() );
		
	}
	public static QueryTemplate parse(String string) {
		return new QueryTemplateParser(string).parse();
	}
}
