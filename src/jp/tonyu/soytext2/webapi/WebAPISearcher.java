package jp.tonyu.soytext2.webapi;

import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.Function;

import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.js.JSSession;
import jp.tonyu.soytext2.js.MapScriptable;

public class WebAPISearcher implements Wrappable {
	WebAPI api;
	public WebAPISearcher(WebAPI api) {
		this.api=api;
	}
	Map<String, String> params=new HashMap<String, String>();
	public WebAPISearcher q(String key,String value) {
		params.put(key, value);
		return this;
	}
	public void each(final Function iter) throws Exception {
		final JSSession s=JSSession.cur.get();
		api.search(params,new ItemIterator() {
			@Override
			public boolean iterate(Map<String, String> item) {
				MapScriptable i = new MapScriptable(item);
				Object res=s.call(iter, new Object[]{i});
				if (res instanceof Boolean) {
					Boolean b = (Boolean) res;
					if (b) return true;
				}
				return false;
			}
		});
	}
}
