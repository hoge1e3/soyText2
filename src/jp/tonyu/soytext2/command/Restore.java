package jp.tonyu.soytext2.command;

import java.io.IOException;

import jp.tonyu.soytext2.document.SDB;

import org.tmatesoft.sqljet.core.SqlJetException;

public class Restore {
	public static void main(String[] args) throws IOException, SqlJetException {
		Common.parseArgs(args);
		Common.backupDB();
		SDB sdb = Common.getDB();
		sdb.restoreFromNewestJSON();
		Common.closeDB();
		sdb = Common.getDB();
		RestoreFromRealtimeBackup.restore(sdb);
		sdb.close();
	}
}
