package jp.tonyu.soytext2.js;

import jp.tonyu.debug.Log;
import jp.tonyu.js.BuiltinFunc;
import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.search.Query;
import jp.tonyu.soytext2.search.QueryBuilder;
import jp.tonyu.soytext2.search.expr.AttrOperator;
import jp.tonyu.util.Ref;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class DBSearcher implements Wrappable {
	public final DBHelper dbscr;
	public DBSearcher(DBHelper dbscr) {
		super();
		this.dbscr = dbscr;
	}	
	private QueryBuilder qb=QueryBuilder.create(null);
	public void each(Function iter) {
		dbscr.loader.searchByQuery(qb.toQuery(), iter);
	}
	public DBSearcher q(String name, Object value) {
		qb=qb.tmpl(name,value,AttrOperator.ge);		
		return this;
	}
	public DBSearcher qe(String name, Object value) {
		qb=qb.tmpl(name,value,AttrOperator.exact);
		return this;
	}

	public Object find1() {
		final Ref<Object> res=new Ref<Object>();
		dbscr.loader.searchByQuery(qb.toQuery(), new BuiltinFunc() {
			
			@Override
			public Object call(Context cx, Scriptable scope, Scriptable thisObj,
					Object[] args) {
				res.set(args[0]);
				return null;
			}
		});
		return res.get();	
	}
}
