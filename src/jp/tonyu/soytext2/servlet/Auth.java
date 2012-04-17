package jp.tonyu.soytext2.servlet;

import jp.tonyu.soytext2.auth.AuthenticatorList;
import jp.tonyu.util.Context;

public class Auth {
	private String user;
	private final AuthenticatorList a;
	public static final Context<Auth> cur=new Context<Auth>();

	public Auth(AuthenticatorList a) {
		super();
		this.a = a;
	}
	public boolean auth(String user, String pass) {
		//AuthenticatorList a=authenticator();
		if (a!=null && a.check(user, pass)) {
			this.user=user;
			return true;
		}
		return false;
	}
	public String user() {
		String user=(this.user==null?"nobody":this.user); //  currentSession().userName();
		return user;
	}
}
