package jp.tonyu.soytext2.command;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.servlet.Workspace;
import jp.tonyu.util.SFile;


public class BackupToJSON {
	public static void main(String[] args) throws SQLException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, IOException, ClassNotFoundException {
		Workspace w=new Workspace(new SFile("."));
		SDB s= args.length==0 ? s=w.getPrimaryDB() : w.getDB(args[0]) ;
		Set<String> ids = s.backupToJSON();
		SFile rbd=s.realtimeBackupDir();
		s.close();
		for (SFile rbf:rbd) {
			if (ids.contains( rbf.name() )) rbf.moveAsBackup("backup");
		}
	}
}
