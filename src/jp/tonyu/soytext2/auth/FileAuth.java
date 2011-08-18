package jp.tonyu.soytext2.auth;

public class FileAuth implements Authentificator {
	@Override
	public boolean check(String username, String password) {
		return true;
	}
}
