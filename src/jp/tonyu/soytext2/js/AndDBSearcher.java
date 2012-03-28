package jp.tonyu.soytext2.js;

import jp.tonyu.debug.Log;
import jp.tonyu.js.BuiltinFunc;
import jp.tonyu.js.ContextRunnable;
import jp.tonyu.js.Scriptables;
import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.document.IndexRecord;
import jp.tonyu.soytext2.search.AndQueryBuilder;
import jp.tonyu.soytext2.search.Query;
import jp.tonyu.soytext2.search.QueryBuilder;
import jp.tonyu.soytext2.search.expr.AttrOperator;
import jp.tonyu.util.Ref;
import jp.tonyu.util.Resource;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

public class AndDBSearcher implements Wrappable {
	public final DBHelper dbscr;
	public AndDBSearcher(DBHelper dbscr) {
		super();
		this.dbscr = dbscr;
		qb=new AndQueryBuilder();
	}
	private AndQueryBuilder qb;
	public void each(Function iter) {
		dbscr.loader.searchByQuery(qb.toQuery(), iter);
	}
	public Object template(final Function tmpl) {
		Scriptable r=(Scriptable)DocumentLoader.curJsSesssion().eval("dbtmp",
				Resource.text(DBTemplate.class,".js"));
		final Function add=(Function)ScriptableObject.getProperty(r, "add");
		dbscr.loader.searchByQuery(qb.toQuery(), new BuiltinFunc() {

			@Override
			public Object call(Context cx, Scriptable scope, Scriptable thisObj,
					Object[] args) {
				Object res=tmpl.call(cx, scope, thisObj, args);
				if (res==null || res instanceof Undefined) return true;
				add.call(cx, scope, thisObj, new Object[]{res});
				return false;
			}
		});
		return ScriptableObject.getProperty(r, "node");
	}
	public AndDBSearcher q(String name, Object value) {
		qb=qb.attr(name,value,AttrOperator.ge);
		return this;
	}
	public AndDBSearcher q(String name) {
		qb.keyword(name);
		return this;
	}
	public AndDBSearcher qe(String name, Object value) {
		qb=qb.attr(name,value,AttrOperator.exact);
		return this;
	}

	public Object find1() {
		final Ref<Object> res=new Ref<Object>();
		Query query = qb.toQuery();
		Log.d(this, "Find1 : "+query);
		dbscr.loader.searchByQuery(query, new BuiltinFunc() {

			@Override
			public Object call(Context cx, Scriptable scope, Scriptable thisObj,
					Object[] args) {
				res.set(args[0]);
				Log.d(this,"Find 1: found "+args[0]);
				return true;
			}
		});
		if (res.isSet()) return res.get();
		return null;
	}
	public AndDBSearcher backlinks(String docId) {
		qb.backlinks(docId);// attr(IndexRecord.INDEX_REFERS, value, AttrOperator.exact);
		return this;
	}
	public AndDBSearcher is(String klassId) {
		qb.instof(klassId);
		return this;
	}
}
