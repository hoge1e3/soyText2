package jp.tonyu.soytext2.command;

import java.io.IOException;
import java.sql.SQLException;

import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.servlet.Workspace;
import jp.tonyu.util.SFile;

public class Common {
	static String dbid;
	static Workspace workspace;
	static void parseArgs(String []args) throws IOException, SQLException {
		workspace=new Workspace(new SFile("."));
		dbid=(args.length==0?workspace.getPrimaryDBID():args[0]);
	}
	static private SDB _sdb;
	static SDB getDB() throws SQLException, IOException, ClassNotFoundException {
		if (_sdb!=null) return _sdb;
		return _sdb=workspace.getDB(dbid);
	}
	static void backupDB() throws IOException {
		SFile dbFile = workspace.getDBFile(dbid);//  new SFile( dbFilef );
		if (dbFile.exists()) {
			boolean res=dbFile.moveAsBackup("backup"); // can not move to other dir
			if (!res) {
				throw new IOException("Move "+dbFile+" fail");
			}
		}
	}
	static void closeDB() throws SQLException, IOException, ClassNotFoundException {
		workspace.closeDB(dbid);
		_sdb=null;

	}
}
