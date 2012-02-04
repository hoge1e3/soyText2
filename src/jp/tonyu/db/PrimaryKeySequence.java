package jp.tonyu.db;

import jp.tonyu.soytext2.document.SDB;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public class PrimaryKeySequence {
	int lastNumber;
	public PrimaryKeySequence(final SqlJetHelper db,final SqlJetTableHelper tbl) throws SqlJetException {
		super();
		db.readTransaction(new DBAction() {
			
			@Override
			public void run(SqlJetDb db) throws SqlJetException {
				ISqlJetCursor c = tbl.order(null);
				lastNumber=0;
				if (c.last()) {
					lastNumber=(int) c.getInteger("id");
				}			
			}
		},-1);
	}
	public int next() {
		lastNumber++;
		return lastNumber;
	}
}
