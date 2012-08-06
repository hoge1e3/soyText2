package jp.tonyu.db;

import java.sql.SQLException;


public abstract class WriteAction extends DBAction {
    public abstract void run(JDBCHelper jdbcHelper) throws NotInWriteTransactionException, SQLException;
}
