package jp.tonyu.soytext2.servlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.tmatesoft.sqljet.core.SqlJetException;

import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.util.SFile;

public class Workspace {
	public static final String PRIMARY_DBID_TXT = "primaryDbid.txt";
	SFile home;
	public SFile dbHome() {
		SFile res=home.rel("db");
		if (res.exists()) return res;
		return home;
	}
	public SDB getDB(String dbid) throws SqlJetException,IOException {
		if (getPrimaryDBID().equals(dbid)) return getPrimaryDB();
		SFile db=dbHome().rel(dbid);
		if (!db.exists()) return null;
		SFile f=db.rel("main.db");
		return dbFromFile(f);
	}
	public SDB getPrimaryDB() throws SqlJetException,IOException {
		SFile f=dbHome().rel(getPrimaryDBID());
		return dbFromFile(f);
	}
	Map<File, SDB> cache=new HashMap<File, SDB>();
	private SDB dbFromFile(SFile f) throws SqlJetException {
		File ff=f.javaIOFile();
		SDB res=cache.get(ff);
		if (res!=null) return res;
		res=new SDB(ff);
		cache.put(ff, res);
		return res;
	}
	public String getPrimaryDBID() throws IOException {
		SFile id=dbHome().rel(PRIMARY_DBID_TXT);
		return id.text();
	}
}
