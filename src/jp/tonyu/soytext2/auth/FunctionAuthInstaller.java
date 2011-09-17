package jp.tonyu.soytext2.auth;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;

import jp.tonyu.js.ContextRunnable;
import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.js.JSSession;

public class FunctionAuthInstaller implements Wrappable {
	public void install(AuthenticatorList ls, final Function f) {
		ls.install(new Authenticator() {
			
			@Override
			public boolean check(final String username, final String password) {
				Object r=JSSession.withContext(new ContextRunnable() {
					
					@Override
					public Object run(Context cx) {
						return f.call(cx, f, f, new Object[]{username,password});
					}
				});
				if (r instanceof Boolean) {
					Boolean b = (Boolean) r;
					return b;
				}
				return false;
			}
		});
	}
}
