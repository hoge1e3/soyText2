package jp.tonyu.soytext2.document;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


import jp.tonyu.db.DBAction;
import jp.tonyu.db.JDBCHelper;
import jp.tonyu.db.JDBCRecordCursor;
import jp.tonyu.db.JDBCTable;
import jp.tonyu.db.NotInReadTransactionException;
import jp.tonyu.db.NotInWriteTransactionException;
import jp.tonyu.db.PrimaryKeySequence;
import jp.tonyu.db.ReadAction;
import jp.tonyu.db.WriteAction;
import jp.tonyu.debug.Log;
import java.sql.SQLException;

public class LogManager {
	int lastNumber;
	SDB sdb;
	public LogManager(final SDB sdb) throws SQLException, NotInReadTransactionException {
		super();
		this.sdb=sdb;
		lastNumber=new PrimaryKeySequence(sdb.logTable()).current();
	}
	public void printAll() {
		try {
			sdb.readTransaction(new ReadAction () {
				@Override
				public void run(JDBCHelper db) throws SQLException, NotInReadTransactionException {
					JDBCRecordCursor<LogRecord> c = sdb.logTable().all();
					while (!c.next()) 	{
						LogRecord r = c.fetch();
						Log.d("LOG", r);
						c.next();
					}
					c.close();
				}
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	Map<Integer, LogRecord> cache=new HashMap<Integer, LogRecord>(); //TODO:Cache
	public LogRecord byId(final int id) throws SQLException, NotInReadTransactionException {
		if (!cache.containsKey(id)) {
			/*sdb.readTransaction(new ReadAction() {

				@Override
				public void run(JDBCHelper db) throws SQLException, NotInReadTransactionException {*/
					JDBCRecordCursor<LogRecord> cur = sdb.logTable().lookup("id", id);
					if (cur.next()) {
						LogRecord res = cur.fetch();
						cache.put(id, res);
					}
					cur.close();
			/*	}
			});*/
		}
		return cache.get(id);
	}
	/*public void all(final LogAction action) {
		try {
			sdb.readTransaction(new DBAction() {

				@Override
				public void run(JDBCHelper db) throws SQLException {
					JDBCRecordCursor<LogRecord> c = sdb.logTable().all();
					while (c.next()) {
						LogRecord l = c.fetch();
						if (action.run(l)) break;
					}
					c.close();

				}
			},-1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}*/
	/*private void fromCursor(ISqlJetCursor cur, LogRecord res) throws SQLException {
		res.action=cur.getString("action");
		res.date=cur.getString("date");
		res.target=cur.getString("target");
		res.option=cur.getString("option");
	}*/
	public synchronized LogRecord create() {
		lastNumber++;
		LogRecord res=LogRecord.create(lastNumber);
		res.date=new Date().toString();
		return res;
	}
	public void liftUpLastNumber(int n) throws SQLException, NotInWriteTransactionException {
		if (n>lastNumber) setLastNumber(n);
	}
	public void setLastNumber(int n) throws SQLException, NotInWriteTransactionException {
		lastNumber=n-1;
		write("setLastNumber","");
	}
	public LogRecord write(String action, String target) throws SQLException, NotInWriteTransactionException {
		LogRecord l=create();
		l.action=action;
		l.target=target;
		save(l);
		return l;
	}
	public void save(final LogRecord log) throws SQLException, NotInWriteTransactionException {
		/*sdb.writeTransaction(new WriteAction() {

			@Override
			public void run(JDBCHelper db) throws SQLException, NotInWriteTransactionException {*/
				JDBCTable<LogRecord> t=sdb.logTable();
				JDBCRecordCursor<LogRecord> cur=t.lookup("id", log.id);
				if (cur.next()) {
				    t.update(log);
				} else {
			    	t.insert(log);
			    }
                cur.close();
	/*}
		},-1);*/
	}
	public void importLog(LogRecord curlog) throws SQLException, NotInWriteTransactionException {
		save(curlog);
	}
}
