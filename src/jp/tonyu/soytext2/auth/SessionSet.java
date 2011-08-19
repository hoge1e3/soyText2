package jp.tonyu.soytext2.auth;

import java.util.HashMap;

import jp.tonyu.debug.Log;

public class SessionSet {
	public static HashMap<String, Session> set=new HashMap<String, Session>();

	public static Session get(String id) {
		Session session = set.get(id);
		Log.d("SessionSet", "Get "+id+"="+session);
		return session;
	}

	public static Session put(Session session) {
		set.put(session.id(), session);
		Log.d("SessionSet", "Register "+session.id()+"="+session);
		return session;
	}

	public static Session create(String user) {
		Session s = new Session(user);
		return put(s);
	}
	
}
