package jp.tonyu.soytext2.auth;

public interface Authentificator {

	public abstract boolean check(String username, String password);

}