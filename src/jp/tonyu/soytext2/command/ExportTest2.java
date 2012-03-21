package jp.tonyu.soytext2.command;
import java.io.File;

import org.tmatesoft.sqljet.core.SqlJetException;

import jp.tonyu.soytext.Origin;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.document.backup.Exporter;
import jp.tonyu.soytext2.document.backup.Exporter2;
import jp.tonyu.util.TDate;


public class ExportTest2 {
	public static void main(String[] args) throws Exception {
		SDB s=new SDB(new File("db/main.db"));//, SDB.UID_EXISTENT_FILE);
		//new Exporter(s, new File("db/main.db.txt"));
		String d=new TDate().toString("yyyy_MMdd_hh_mm_ss");
		new Exporter2(s, new File("db/main.db."+d+".txt")).export();
		s.close();//aaa
	}
}
