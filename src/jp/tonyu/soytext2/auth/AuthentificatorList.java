package jp.tonyu.soytext2.auth;

import java.util.Vector;

public class AuthentificatorList implements Authentificator {
	public static Authentificator alist=prepare();
	Vector<Authentificator> auths=new Vector<Authentificator>();
	@Override
	public boolean check(String username, String password) {
		for (Authentificator a: auths) {
			if (a.check(username, password)) return true;
		}
		return false;
	}
	public void install(Authentificator a) {
		auths.add(a);
	}
	
	public static AuthentificatorList prepare() {
		AuthentificatorList res=new AuthentificatorList();
		res.install(new FileAuth());
		res.install(new Authentificator() {
			
			@Override
			public boolean check(String username, String password) {
				return "all".equals(username) && "oll".equals(password);
			}
		});
		return res;
	}
}
