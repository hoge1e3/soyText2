import java.io.File;

import org.tmatesoft.sqljet.core.SqlJetException;

import jp.tonyu.soytext2.db.SDB;
import jp.tonyu.soytext2.document.Document;


public class Test {
	public static void main(String[] args) throws SqlJetException {
		SDB s=new SDB(new File("test.db"));
		Document d=s.newDocument();
		
	}
}
