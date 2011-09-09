package jp.tonyu.soytext2.auth;

public class LDAPAuthFactory {
	public LDAPAuth create(String host, String binddn, String bindpw, String base) {
		return new LDAPAuth(host, binddn, bindpw, base);
	}
}
