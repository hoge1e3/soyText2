package jp.tonyu.soytext2.auth;

import jp.tonyu.js.Wrappable;
import jp.tonyu.util.Context;

public class Session implements Wrappable {
	//public static final Context<Session> cur=new Context<Session>();
	public static final Session NOBODY = new Session("nobody");
	String userName;
	String id=Math.random()+"";
	public Session(String u) {userName=u;}
	public String id() {
		return id;
	}
	public String userName() {
		return userName;
	}
}
