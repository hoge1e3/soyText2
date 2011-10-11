package jp.tonyu.soytext2.auth;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import jp.tonyu.debug.Log;
import jp.tonyu.js.Wrappable;

public class AuthenticatorList implements Authenticator, Wrappable {
	//public static Authentificator alist=prepare();
	Vector<Authenticator> auths=new Vector<Authenticator>();
	Set<String> roots=new HashSet<String>();
	@Override
	public boolean check(String username, String password) {
		for (Authenticator a: auths) {
			if (a.check(username, password)) return true;
		}
		return false;
	}
	public void install(Authenticator a) {
		Log.d(this, "Installed - "+a);
		auths.add(a);
	}
	public void addRootUser(String userName) {
		roots.add(userName);
	}
	public boolean isRootUser(String userName) {
		return roots.contains(userName);
	}
	public static AuthenticatorList repare() {
		AuthenticatorList res=new AuthenticatorList();
		res.install(new AnyAuth(""));
		/*res.install(new Authentificator() {
			
			@Override
			public boolean check(String username, String password) {
				return "all".equals(username) && "oll".equals(password);
			}
		});*/
		return res;
	}
}
