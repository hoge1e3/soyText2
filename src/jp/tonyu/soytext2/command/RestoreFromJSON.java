package jp.tonyu.soytext2.command;
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
import jp.tonyu.soytext2.servlet.Workspace;
import jp.tonyu.util.SFile;


public class RestoreFromJSON {
	public static void main(String[] args) throws SqlJetException, IllegalArgumentException, NoSuchFieldException, IllegalAccessException, IOException {
		/*Workspace workspace=new Workspace(new SFile("."));
		String dbid=(args.length==0?workspace.getPrimaryDBID():args[0]);

		SFile dbFile = workspace.getDBFile(dbid);//  new SFile( dbFilef );
		if (dbFile.exists()) {
			boolean res=dbFile.moveAsBackup("backup"); // can not move to other dir
			if (!res) {
				System.out.println("Move fail");
				return;
			}
		}*/

		Common.parseArgs(args);
		Common.backupDB();
		SDB s=Common.getDB();
		s.restoreFromNewestJSON();
		s.close();
	}
}
