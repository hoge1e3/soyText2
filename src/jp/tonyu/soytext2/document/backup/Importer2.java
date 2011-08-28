package jp.tonyu.soytext2.document.backup;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import jp.tonyu.db.DBAction;
import jp.tonyu.db.SqlJetHelper;
import jp.tonyu.db.SqlJetRecord;
import jp.tonyu.db.SqlJetTableHelper;

public class Importer2 {
	Map<String, SqlJetRecord> m=new HashMap<String, SqlJetRecord>();
	File src; SqlJetHelper db;
	public Importer2(SqlJetHelper db,File src) {
		this.src=src;
		this.db=db;
	}
	Pattern ver=Pattern.compile("version=(\\d+)");
	public void importRecords() throws Exception {
		db.writeTransaction(new DBAction() {
			
			@Override
			public void run(SqlJetDb db2) throws SqlJetException {
				Scanner s;
				try {
					s = new Scanner(src);
					String vline=s.nextLine();
					Matcher ma = ver.matcher(vline);
					int version=1;
					if (ma.find()) {
						version=Integer.parseInt(ma.group(1));
					}
					for (SqlJetRecord r:db.tables(version)) {
						m.put(r.tableName(), r);
					}
					String tableName=SqlJetRecord.firstTableName(s);
					while (tableName!=null) {
						SqlJetRecord curRec=m.get(tableName);
						SqlJetTableHelper curTbl=db.table(tableName);
						tableName=curRec.importRecord(s);
						curRec.insertTo(curTbl);
					}				
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}, -1);		
	}
}
