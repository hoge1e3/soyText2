package jp.tonyu.soytext2.js;

import jp.tonyu.debug.Log;
import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.document.IndexRecord;
import jp.tonyu.soytext2.search.QueryBuilder;
import jp.tonyu.soytext2.search.expr.AttrOperator;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

public class DBHelper implements Wrappable{
	public final DocumentLoader loader;

	public Object q(Object value) {
		if (value instanceof String) {
			String qstr = (String) value;
			return new DBSearcher(this,qstr);
		} else if (value instanceof DocumentScriptable){
			DocumentScriptable ds=(DocumentScriptable)value;
			return new AndDBSearcher(this).is(ds.id());
		} else {
			Log.die("Not a query value - "+value);
			return null;
		}
	}
	public AndDBSearcher is(Object value) {
		if (value instanceof DocumentScriptable){
			DocumentScriptable ds=(DocumentScriptable)value;
			return new AndDBSearcher(this).is(ds.id());
		}
		return null;
	}
	public AndDBSearcher backlinks(Object value) {
		return new AndDBSearcher(this).backlinks(value);
	}
	public AndDBSearcher q(String name, Object value) {
		return new AndDBSearcher(this).q(name, value);
	}
	public DBHelper(DocumentLoader loader) {
		super();
		this.loader = loader;
	}
	public AndDBSearcher qe(String name, Object value) {
		return new AndDBSearcher(this).qe(name, value);
	}

	public Object find(Function iter) {
		loader.search("", null, iter);
		return this;
	}

	public Object insert(Scriptable obj) {
		DocumentScriptable d = loader.newDocument(obj);		
		return d;
	}
	public Object byId(String id) {
		return loader.byId(id);
	}
	public void debug(Object o) {
		Log.d("db.debug", o);
	}
	public Object byIdOrCreate(String id) {
		Object res=loader.byId(id);
		if (res!=null) return res;
		return loader.newDocument(id);
		
	}
	public String getContent(DocumentScriptable d) {
		return d.getDocument().content;
	}
	public void setContentAndSave(DocumentScriptable d,String newContent) {
		d.setContentAndSave(newContent);
	}
	public String dbid() {
		return loader.getDocumentSet().getDBID();
	}
}
