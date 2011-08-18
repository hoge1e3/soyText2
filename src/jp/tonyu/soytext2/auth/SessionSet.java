package jp.tonyu.soytext2.auth;

import java.util.HashMap;

public class SessionSet {
	public static HashMap<String, Session> set=new HashMap<String, Session>();

	public static Session get(Object key) {
		return set.get(key);
	}

	public static Session put(String key, Session value) {
		set.put(key, value);
		return value;
	}

	public static Session create(String user) {
		Session s = new Session(user);
		return put(user,s);
	}
	
}
