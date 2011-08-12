import java.io.File;

import org.tmatesoft.sqljet.core.SqlJetException;

import jp.tonyu.soytext.Origin;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.document.backup.Exporter;


public class ExportTest {
	public static void main(String[] args) throws Exception {
		SDB s=new SDB(new File("db/main.db"), Origin.uid);
		new Exporter(s, new File("db/main.db.txt"));
		s.close();
	}
}
