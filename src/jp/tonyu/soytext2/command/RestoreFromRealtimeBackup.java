package jp.tonyu.soytext2.command;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import jp.tonyu.db.NotInWriteTransactionException;
import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.js.DocumentLoader;
import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.servlet.Workspace;
import jp.tonyu.util.SFile;

public class RestoreFromRealtimeBackup {
	public static void main(String[] args) throws Exception  {
		Workspace workspace=new Workspace(new SFile("."));
		String dbid=(args.length==0?workspace.getPrimaryDBID():args[0]);
		SDB s=workspace.getDB(dbid);
		restore(s);
		s.close();
	}
	// Why this is a method of SDB? because it creates a DocumentLoader.
	public static void restore(SDB s) throws SQLException, NotInWriteTransactionException {
		SFile rd=s.realtimeBackupDir();
		DocumentRecord d=new DocumentRecord();
		Set<String> updated=new HashSet<String>();
		for (SFile r:rd) {
			try {
				if (r.isDir()) continue;
				s.restoreFromRealtimeBackup(r, updated);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		DocumentLoader l=new DocumentLoader(s);
		for (String id:updated) {
			Log.d("Restore", "Refresh index of "+id);
			DocumentScriptable ds = l.byIdOrNull(id);
			if (ds==null) Log.d("Restore", "Why!? "+id+" is not exist!!");
			ds.refreshIndex();
		}
		s.close();
	}
}
