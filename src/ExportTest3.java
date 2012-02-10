import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import net.arnx.jsonic.JSON;

import org.tmatesoft.sqljet.core.SqlJetException;

import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.servlet.SMain;
import jp.tonyu.util.SFile;
import jp.tonyu.util.TDate;


public class ExportTest3 {
	public static void main(String[] args) throws SqlJetException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, IOException {
		SDB s=new SDB(SMain.getNewestDBFile(new SFile("db")));
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
