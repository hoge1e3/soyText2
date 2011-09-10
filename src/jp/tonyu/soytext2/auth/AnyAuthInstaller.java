package jp.tonyu.soytext2.auth;

import jp.tonyu.js.Wrappable;

public class AnyAuthInstaller implements Wrappable {
	public void install(AuthenticatorList list, String commonPassword) {
		list.install( new AnyAuth(commonPassword) );
	}
}
