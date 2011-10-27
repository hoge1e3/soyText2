import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.tmatesoft.sqljet.core.SqlJetException;

import jp.tonyu.soytext.Origin;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.document.backup.Exporter;
import jp.tonyu.soytext2.document.backup.Importer;
import jp.tonyu.soytext2.document.backup.Importer2;
import jp.tonyu.util.SFile;
import jp.tonyu.util.TDate;


public class ImportTest2 {

	public static void main(String[] args) throws Exception {
		
		SFile dbDir=new SFile("db");
		SFile dbFile = dbDir.rel("main.db");
		if (dbFile.exists()) dbFile.moveAsBackup("backup");
		
		
		SFile src=null;
		for (SFile txt:dbDir) {
			if (!txt.name().endsWith(".txt")) continue;
			if (src==null || txt.lastModified()>src.lastModified()) {
				src=txt;
			}
		}
		System.out.println("Import from "+src);
		
		
		SDB s=new SDB(dbFile.javaIOFile());//, SDB.UID_IMPORT);
		new Importer2(s,src.javaIOFile()).importRecords();
		s.close();
		
		
		
		/*SDB s=new SDB(new File("db/main.db"), SDB.UID_IMPORT);
		new Importer(s).importDocuments(new File("db/main.db.3.txt"));
		s.close();*/
	}
}
