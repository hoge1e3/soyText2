package jp.tonyu.db;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class DBAction {
    //public abstract void run(JDBCHelper db) throws SQLException;
	public void afterCommit(JDBCHelper db){
	}
	public void afterRollback(JDBCHelper db) {
	}
}
