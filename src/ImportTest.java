import java.io.File;

import org.tmatesoft.sqljet.core.SqlJetException;

import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.document.backup.Exporter;
import jp.tonyu.soytext2.document.backup.Importer;


public class ImportTest {
	public static void main(String[] args) throws Exception {
		SDB s=new SDB(new File("db/main.db"));
		new Importer(s).importDocuments(new File("db/main.db.txt"));
		s.close();
	}
}
