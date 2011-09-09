package jp.tonyu.soytext2.auth;

public interface Authenticator {

	public abstract boolean check(String username, String password);

}