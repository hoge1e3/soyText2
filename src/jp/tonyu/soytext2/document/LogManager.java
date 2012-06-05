package jp.tonyu.soytext2.document;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


import jp.tonyu.db.DBAction;
import jp.tonyu.db.JDBCHelper;
import jp.tonyu.db.JDBCRecordCursor;
import jp.tonyu.db.PrimaryKeySequence;
import jp.tonyu.debug.Log;
import java.sql.SQLException;

public class LogManager {
	int lastNumber;
	SDB sdb;
	public LogManager(final SDB sdb) throws SQLException {
		super();
		this.sdb=sdb;
		lastNumber=PrimaryKeySequence.create(sdb.logTable()).current();
	}
	public void printAll() {
		try {
			sdb.readTransaction(new DBAction () {
				@Override
				public void run(JDBCHelper db) throws SQLException {
					JDBCRecordCursor<LogRecord> c = sdb.logTable().all();
					while (!c.next()) 	{
						LogRecord r = c.fetch();
						Log.d("LOG", r);
						c.next();
					}
					c.close();
				}
			}, -1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	Map<Integer, LogRecord> cache=new HashMap<Integer, LogRecord>(); //TODO:Cache
	public LogRecord byId(final int id) throws SQLException {
		if (!cache.containsKey(id)) {
			sdb.readTransaction(new DBAction() {

				@Override
				public void run(JDBCHelper db) throws SQLException {
					JDBCRecordCursor<LogRecord> cur = sdb.logTable().lookup("id", id);
					if (cur.next()) {
						LogRecord res = cur.fetch();
						cache.put(id, res);
					}
					cur.close();
				}
			},-1);
		}
		return cache.get(id);
	}
	public void all(final LogAction action) {
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
	}
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
	public void liftUpLastNumber(int n) {
		if (n>lastNumber) setLastNumber(n);
	}
	public void setLastNumber(int n) {
		lastNumber=n-1;
		write("setLastNumber","");
	}
	public LogRecord write(String action, String target) {
		LogRecord l=create();
		l.action=action;
		l.target=target;
		save(l);
		return l;
	}
	public void save(final LogRecord log) {
		sdb.writeTransaction(new DBAction() {

			@Override
			public void run(JDBCHelper db) throws SQLException {
				SqlJetTableHelper t = sdb.logTable();
				ISqlJetCursor cur = t.lookup(null, log.id);

				if (!cur.eof()) {
					log.update(cur);
					//cur.update(log.id,log.date,log.action,log.target,log.option);
			    } else {
			    	log.insertTo(t);
			    	//t.insert(log.id,log.date,log.action,log.target,log.option);
			    }
			}
		});
	}
	public void importLog(LogRecord curlog) {
		save(curlog);
	}
}
