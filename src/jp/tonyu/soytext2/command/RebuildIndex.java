package jp.tonyu.soytext2.command;

import java.io.IOException;
import java.sql.SQLException;

import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.js.DocumentLoader;
import jp.tonyu.soytext2.servlet.Workspace;
import jp.tonyu.util.SFile;

public class RebuildIndex {
	public static void main(String[] args) throws Exception {
		Workspace workspace=new Workspace(new SFile("."));
		String dbid=(args.length==0?workspace.getPrimaryDBID():args[0]);

		/*File f;
		if (args.length>0) {
			f=new File(args[0]);
		} else {
			f=SMain.getNewestPrimaryDBFile(new SFile("db"));
		}*/
		SDB s=workspace.getDB(dbid);// new SDB(f);
		DocumentLoader d = new DocumentLoader(s);
		d.rebuildIndex();
		s.close();
	}
}
