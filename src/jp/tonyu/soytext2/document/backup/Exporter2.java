package jp.tonyu.soytext2.document.backup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import jp.tonyu.db.DBAction;
import jp.tonyu.db.SqlJetRecord;
import jp.tonyu.db.SqlJetTableHelper;
import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.document.DocumentAction;
import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.document.LogAction;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.document.LogRecord;
import jp.tonyu.soytext2.document.UIDRecord;
import jp.tonyu.util.Literal;

public class Exporter2 {
	SDB db;File out;
	PrintStream p;
	public Exporter2(SDB db, File out) {
		this.db=db;
		this.out=out;
	}
	/*public <T extends SqlJetRecord> void  exportTable(Class<T> klass) throws Exception {
		SqlJetTableHelper table = db.table(klass);
		exportTable(klass, table);
	}*/
	private void exportTable(SqlJetRecord uidRecord) throws Exception {
		exportTable(uidRecord.tableName(), uidRecord);
		
	}
	public <T extends SqlJetRecord> void  exportTable(String actTable, SqlJetRecord r) throws Exception {
		SqlJetTableHelper table = db.table(actTable);
		exportTable(r, table);
	}
	private <T extends SqlJetRecord> void exportTable(SqlJetRecord r, SqlJetTableHelper table)
			throws SqlJetException, InstantiationException,
			IllegalAccessException {
		ISqlJetCursor cur = table.order();
		while (!cur.eof()) {
			r.fetch(cur);			
			r.export(p);
			cur.next();
		}
		cur.close();
	}
	public void export() throws Exception {
		p=new PrintStream(out);
		p.println("version=1");
		db.readTransaction(new DBAction() {
			
			@Override
			public void run(SqlJetDb db) throws SqlJetException {
				try {
					/*exportTable("Document_1",new DocumentRecord());
					exportTable("Log_1",new LogRecord());*/
					exportTable(new UIDRecord());
					exportTable(new DocumentRecord());
					exportTable(new LogRecord());
				} catch (Exception e) {
					e.printStackTrace();
					Log.die("Excep: "+e);
				}
			}


		}, -1);
		p.close();
	}
}
