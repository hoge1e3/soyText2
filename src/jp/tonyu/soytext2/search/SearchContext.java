package jp.tonyu.soytext2.search;

import java.util.Hashtable;
import java.util.Map;
import java.util.SortedSet;

import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.search.expr.AttrExpr;


public class SearchContext {
	final Map<String, TemplateMatchResult> tmpl=new Hashtable<String, TemplateMatchResult>();
    final DocumentScriptable toBeSearched;
    final QueryTemplate queryTmpl;
   // DocumentRef documentRef;
    void init(SortedSet<AttrExpr> templateValues) {
		for (AttrExpr a:templateValues) {
			if (!queryTmpl.contains(a.name)) Log.die("(query "+queryTmpl+") does not have '"+a.name+"' as template key name");
			tmpl.put(a.name, new TemplateMatchResult(a));
		}
    }
	private SearchContext(DocumentScriptable toBeSearched, QueryTemplate query, SortedSet<AttrExpr> templateValues) {
		this.toBeSearched=toBeSearched;
		this.queryTmpl=query;//Debug.notNull(query);
		
		init(templateValues);
	}
	public SearchContext(DocumentScriptable toBeSearched, Query query) {
		this(toBeSearched, query.getFilterTemplate(), query.attrs);
	}
		
	/*public SearchContext(DocumentRef d, SortedSet<AttrExpr> templateValues) {
		init(templateValues);
		documentRef=d;
	}*/
	public TemplateMatchResult getTemplateMatchResult(String name) {
		return tmpl.get(name);
	}
	public QueryResult matches() {
		//Debug.notNull(query);
		QueryResult res=queryTmpl.cond.matches(toBeSearched, this);
		
		if (res.templateMatched && !templateFilled()) {
			Log.die(queryTmpl+": still null "+tmpl.values()+" for document "+toBeSearched);
		}
		return res;
	}
	public boolean templateFilled() {
		for (TemplateMatchResult a:tmpl.values()) {
			if (a.actualValue==null) {
				return false;
			}
		}
		return true;
	}
/*	public CacheDocumentRef matchedDocumentRef() {
		CacheDocumentRef r=new CacheDocumentRef(toBeSearched);
		for (TemplateMatchResult a:tmpl.values()) {
			r.put(a.cond.name, a.actualValue);
		}
		return r;
	}*/
}
