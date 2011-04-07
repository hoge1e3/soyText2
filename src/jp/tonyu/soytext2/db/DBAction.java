package jp.tonyu.soytext2.db;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public abstract class DBAction {
	public abstract void run(SqlJetDb db) throws SqlJetException;
	public void afterCommit(SqlJetDb db){
	}
}
