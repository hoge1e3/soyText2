import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import jp.tonyu.soytext.Origin;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.document.backup.Importer;
import jp.tonyu.soytext2.servlet.SMain;


public class ImportFromVer1 {

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
		w.println("[Document]");
		while (sc.hasNextLine()) {
			String l=sc.nextLine();
			if (l.startsWith("<pre>") || l.startsWith("</pre>")) continue;
			w.println(l);
			//System.out.println(l);
		}
		sc.close();
		w.close();
		
		DateFormat dfm = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
		File newFile = new File("db/main"+ dfm.format(new Date())+".db");
		//File newFile = SMain.getNewest();
		String uid= Origin.uid;
		SDB s=new SDB(newFile);//,uid);
		Importer i=new Importer(s);
		i.importDocuments(file);
		s.close();
		
		new SMain(uid);
	}

}
