import java.io.File;

import org.tmatesoft.sqljet.core.SqlJetException;

import jp.tonyu.soytext.Origin;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.document.backup.Exporter;
import jp.tonyu.soytext2.document.backup.Importer;
import jp.tonyu.soytext2.document.backup.Importer2;


public class ImportTest2 {
	public static void main(String[] args) throws Exception {
		SDB s=new SDB(new File("db/main.test.db"), SDB.UID_IMPORT);
		new Importer2(s,new File("db/main.db.test.txt")).importRecords();
		s.close();
		
		/*SDB s=new SDB(new File("db/main.db"), SDB.UID_IMPORT);
		new Importer(s).importDocuments(new File("db/main.db.3.txt"));
		s.close();*/
	}
}
