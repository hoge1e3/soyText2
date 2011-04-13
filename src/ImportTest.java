import java.io.File;

import org.tmatesoft.sqljet.core.SqlJetException;

import jp.tonyu.soytext2.db.SDB;
import jp.tonyu.soytext2.document.importing.Importer;


public class ImportTest {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		SDB s=new SDB(new File("test.db"));
		Importer i=new Importer(s);
		i.importDocuments(new File("import/import.js"));
		s.close();
	}

}
