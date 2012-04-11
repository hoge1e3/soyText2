package jp.tonyu.soytext2.command;

import java.io.File;
import java.io.IOException;

import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.js.DocumentLoader;
import jp.tonyu.soytext2.servlet.SMain;
import jp.tonyu.util.SFile;

import org.tmatesoft.sqljet.core.SqlJetException;

public class RebuildIndex {
	public static void main(String[] args) throws SqlJetException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, IOException {
		File f;
		if (args.length>0) {
			f=new File(args[0]);
		} else {
			f=SMain.getNewestPrimaryDBFile(new SFile("db"));
		}
		SDB s=new SDB(f);
		DocumentLoader d = new DocumentLoader(s);
		d.rebuildIndex();
		s.close();
	}
}
