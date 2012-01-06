package jp.tonyu.soytext2.js;

import jp.tonyu.debug.Log;
import jp.tonyu.js.Wrappable;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

public class DBHelper implements Wrappable{
	public final DocumentLoader loader;

	public DBSearcher q(Object value) {
		return new DBSearcher(this,value);
	}
	public DBSearcher q(String name, Object value) {
		return new DBSearcher(this).q(name, value);
	}
	public DBHelper(DocumentLoader loader) {
		super();
		this.loader = loader;
	}
	public DBSearcher qe(String name, Object value) {
		return new DBSearcher(this).qe(name, value);
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
