package jp.tonyu.soytext2.js;

import jp.tonyu.js.Wrappable;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

public class DBHelper implements Wrappable{
	public final DocumentLoader loader;
	
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
}
