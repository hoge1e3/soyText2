package jp.tonyu.soytext2.auth;

import jp.tonyu.js.Wrappable;

public class AnyAuth implements Authenticator {
	String commonPassword;
	public AnyAuth(String commonPassword) {
		this.commonPassword=commonPassword;
	}
	@Override
	public boolean check(String username, String password) {
		return commonPassword.equals(password);
	}
}
