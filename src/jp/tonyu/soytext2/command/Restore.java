package jp.tonyu.soytext2.command;

import java.io.IOException;
import java.sql.SQLException;

import jp.tonyu.soytext2.document.SDB;

public class Restore {
	public static void main(String[] args) throws Exception {
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
