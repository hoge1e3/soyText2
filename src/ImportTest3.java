import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import net.arnx.jsonic.JSON;

import org.tmatesoft.sqljet.core.SqlJetException;

import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.servlet.SMain;
import jp.tonyu.util.SFile;


public class ImportTest3 {
	public static void main(String[] args) throws SqlJetException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, IOException {
		/*SFile dbDir=new SFile("db");
		SFile dbFile = dbDir.rel("main.db");*/
		SDB s=new SDB(SMain.getNewestDBFile(new SFile("db")));
		SFile dbFile=new SFile(s.getFile());
		
		if (dbFile.exists()) {
			boolean res=dbFile.moveAsBackup("backup"); // can not move to other dir
			if (!res) {
				System.out.println("Move fail");
				return;
			}
		}
		/*
		
		SFile src=null;
		for (SFile txt:dbDir) {
			if (!txt.name().endsWith(".json")) continue;
			if (src==null || txt.lastModified()>src.lastModified()) {
				src=txt;
			}
		}
		System.out.println("Import from "+src);
		
		InputStream in = src.inputStream();
		Map b=(Map)JSON.decode(in);
		in.close();*/
		
		//SDB s=new SDB(dbFile.javaIOFile());
		s.restoreFromNewestFile();
		s.close();
	}
}
