package jp.tonyu.soytext2.document.backup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import jp.tonyu.db.DBAction;
import jp.tonyu.db.SqlJetRecord;
import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.document.DocumentAction;
import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.document.LogAction;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.document.LogRecord;
import jp.tonyu.util.Literal;

public class Exporter2 {
	SDB db;File out;
	PrintStream p;
	public Exporter2(SDB db, File out) {
		this.db=db;
		this.out=out;
	}
	public <T extends SqlJetRecord> void  exportTable(Class<T> klass) throws Exception {
		ISqlJetCursor cur = db.table(klass).order();
		T d=klass.newInstance();
		while (!cur.eof()) {
			d.set(cur);			
			d.export(p);
			cur.next();
		}
		cur.close();
	}
	public <T extends SqlJetRecord> void  exportTable(String actTable, Class<T> klass) throws Exception {
		ISqlJetCursor cur = db.table(actTable).order();
		T d=klass.newInstance();
		while (!cur.eof()) {
			d.set(cur);			
			d.export(p);
			cur.next();
		}
		cur.close();
	}
	public void export() throws Exception {
		p=new PrintStream(out);
		db.readTransaction(new DBAction() {
			
			@Override
			public void run(SqlJetDb db) throws SqlJetException {
				try {
					exportTable("Document_1",DocumentRecord.class);
					exportTable("Log_1",LogRecord.class);
				} catch (Exception e) {
					e.printStackTrace();
					Log.die("Excep: "+e);
				}
			}
		}, -1);
		p.close();
	}
}
