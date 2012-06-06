package jp.tonyu.soytext2.command;
import jp.tonyu.soytext2.document.SDB;


public class RestoreFromJSON {
	public static void main(String[] args) throws Exception {
		/*Workspace workspace=new Workspace(new SFile("."));
		String dbid=(args.length==0?workspace.getPrimaryDBID():args[0]);

		SFile dbFile = workspace.getDBFile(dbid);//  new SFile( dbFilef );
		if (dbFile.exists()) {
			boolean res=dbFile.moveAsBackup("backup"); // can not move to other dir
			if (!res) {
				System.out.println("Move fail");
				return;
			}
		}*/

		Common.parseArgs(args);
		Common.backupDB();
		SDB s=Common.getDB();
		s.restoreFromNewestJSON();
		s.close();
	}
}
