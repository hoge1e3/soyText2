package jp.tonyu.soytext2.auth;

public class AnyAuthFactory {
	public AnyAuth create(String commonPassword) {
		return new AnyAuth(commonPassword);
	}
}
