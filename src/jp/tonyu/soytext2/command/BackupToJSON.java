package jp.tonyu.soytext2.command;
import java.io.IOException;

import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.servlet.SMain;
import jp.tonyu.soytext2.servlet.Workspace;
import jp.tonyu.util.SFile;

import org.tmatesoft.sqljet.core.SqlJetException;


public class BackupToJSON {
	public static void main(String[] args) throws SqlJetException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, IOException {
		Workspace w=new Workspace(new SFile("."));
		SDB s= args.length==0 ? s=w.getPrimaryDB() : w.getDB(args[0]) ;
		//SDB s=new SDB(SMain.getNewestPrimaryDBFile(new SFile("db")));
		/*Object b=s.backup();
		JSON json = new JSON();
		json.setPrettyPrint(true);
		String d=new TDate().toString("yyyy_MMdd_hh_mm_ss");
		OutputStream out = new SFile("db/backup/main.db."+d+".json").outputStream();
		json.format(b, out);
		out.close();*/
		s.backupToFile();
		s.close();
	}
}
