package jp.tonyu.soytext2.db;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public interface DBAction {
	public void run(SqlJetDb db) throws SqlJetException;
}
