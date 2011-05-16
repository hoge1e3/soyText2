import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Scanner;

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
		URL u=new URL("http://localhost:3001/exec/110412_045800");
		InputStream in=(InputStream)u.getContent();
		File file = new File("import/import.txt");
		Scanner sc=new Scanner(in);
		PrintWriter w=new PrintWriter(file);
		while (sc.hasNextLine()) {
			String l=sc.nextLine();
			if (l.startsWith("<pre>") || l.startsWith("</pre>")) continue;
			w.println(l);
			//System.out.println(l);
		}
		sc.close();
		w.close();
		
		SDB s=new SDB(new File("main.db"));
		Importer i=new Importer(s);
		i.importDocuments(file);
		s.close();
	}

}
