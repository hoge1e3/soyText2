package jp.tonyu.db;

import java.sql.SQLException;


public abstract class ReadAction extends DBAction {
    public abstract void run(JDBCHelper jdbcHelper) throws NotInReadTransactionException, SQLException;
}
