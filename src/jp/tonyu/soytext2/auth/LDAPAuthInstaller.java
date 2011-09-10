package jp.tonyu.soytext2.auth;

import jp.tonyu.js.Wrappable;

public class LDAPAuthInstaller implements Wrappable {
	public void install(AuthenticatorList list,String host, String binddn, String bindpw, String base) {
		list.install(new LDAPAuth(host, binddn, bindpw, base));
	}
}
