package jp.tonyu.soytext2.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.util.SFile;

public class Workspace {
	public static final String MAIN_DB = "main.db";
	public static final String PRIMARY_DBID_TXT = "primaryDbid.txt";
	SFile home;

	public Workspace(SFile home) {
		super();
		this.home = home;
	}
	public SFile multiDBHome() {
		SFile res=home.rel("db");
		res.mkdirs(true);
		//if (res.exists()) return res;
		return res;
	}
	// if directory not exists returns null, if directory exists and main.db does not exist, main.db is created
	public SDB getDB(String dbid) throws SQLException,IOException, ClassNotFoundException {
		//if (getPrimaryDBID().equals(dbid)) return getPrimaryDB();
		SFile db=singleDBHome(dbid);
		if (!db.exists()) return null;
		SFile f=db.rel(MAIN_DB);
		return dbFromFile(f);
	}
	public SDB getPrimaryDB() throws SQLException,IOException, ClassNotFoundException {
		/*SFile f=dbHome().rel(getPrimaryDBID());
		return dbFromFile(f);*/
		return getDB(getPrimaryDBID());
	}
	static Map<File, SDB> cache=new HashMap<File, SDB>();
	private SDB dbFromFile(SFile f) throws SQLException, ClassNotFoundException {
		File ff=f.javaIOFile();
		SDB res=cache.get(ff);
		if (res!=null) return res;
		res=new SDB(ff);
		cache.put(ff, res);
		return res;
	}
	public String getPrimaryDBID() throws IOException {
		SFile id=primaryDBFile();
		return id.lines()[0];
	}
	private SFile primaryDBFile() {
		return multiDBHome().rel(PRIMARY_DBID_TXT);
	}
	public static final String DB_INIT_PATH = "jp/tonyu/soytext2/servlet/init/db";
	void setupDB() throws IOException {
		if (!isEmpty()) return;

		//final SFile dbDir=dbDir();
		ClassLoader cl=this.getClass().getClassLoader();
		//SFile dbIdFile=dbDir.rel(SDB.PRIMARY_DBID_TXT);
		InputStream in=cl.getResourceAsStream(DB_INIT_PATH+"/"+PRIMARY_DBID_TXT);
		Scanner s=new Scanner(in);
		String primaryDbid=s.nextLine();
		s.close();

		primaryDBFile().text(primaryDbid);
		//dbIdFile.readFrom(in);
		//String dbId=dbIdFile.text();
		SFile dbDir_in=singleDBHome(primaryDbid); //dbDir.rel(dbid);
		SFile dbFile=dbDir_in.rel(MAIN_DB);
		if (!dbFile.exists()) {
			in=cl.getResourceAsStream(DB_INIT_PATH+"/"+MAIN_DB);
			dbFile.readFrom(in);
		}
	}
	public SFile singleDBHome(String dbid) {
		return multiDBHome().rel(dbid);
	}
	public SFile getDBFile(String dbid) {
		return singleDBHome(dbid).rel(MAIN_DB);
	}
	private boolean isEmpty() {
		return !primaryDBFile().exists();
	}
	public void closeDB(String dbid) throws SQLException, IOException, ClassNotFoundException {
		getDB(dbid).close();
		cache.remove(getDBFile(dbid).javaIOFile());

	}

}
