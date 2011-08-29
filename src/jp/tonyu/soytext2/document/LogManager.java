package jp.tonyu.soytext2.document;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import jp.tonyu.db.DBAction;
import jp.tonyu.db.SqlJetTableHelper;
import jp.tonyu.debug.Log;

public class LogManager {
	int lastNumber;
	SDB sdb;
	public LogManager(final SDB sdb) throws SqlJetException {
		super();
		this.sdb=sdb;
		sdb.readTransaction(new DBAction() {
			
			@Override
			public void run(SqlJetDb db) throws SqlJetException {
				SqlJetTableHelper t = sdb.logTable();
				ISqlJetCursor c = t.order(null);
				lastNumber=0;
				if (c.last()) {
					lastNumber=(int) c.getInteger("id");
				}			
			}
		},-1);
	}
	public void printAll() {
		try {
			sdb.readTransaction(new DBAction () {
				@Override
				public void run(SqlJetDb db) throws SqlJetException {
					SqlJetTableHelper t = sdb.logTable();
					ISqlJetCursor c = t.order(null);
					while (!c.eof()) {
						Log.d("LOG", c.getValue("id")+","+c.getValue("date")+","+c.getValue("action")+","+c.getValue("target") );
						c.next();
					}
					c.close();
				}
			}, -1);
		} catch (SqlJetException e) {
			e.printStackTrace();
		}
	}
	Map<Integer, LogRecord> cache=new HashMap<Integer, LogRecord>(); //TODO:Cache
	public LogRecord byId(final int id) throws SqlJetException {
		if (!cache.containsKey(id)) {
			sdb.readTransaction(new DBAction() {

				@Override
				public void run(SqlJetDb db) throws SqlJetException {
					SqlJetTableHelper t = sdb.logTable();
					ISqlJetCursor cur = t.lookup(null, id);
					LogRecord res=LogRecord.create(id,sdb);
					if (!cur.eof()) {
						cache.put(id, res);
						fromCursor(cur, res);
					}

				}
			},-1);
		}
		return cache.get(id);
	}
	public void all(final LogAction action) {
		try {
			sdb.readTransaction(new DBAction() {

				@Override
				public void run(SqlJetDb db) throws SqlJetException {
					SqlJetTableHelper t = sdb.logTable();
					ISqlJetCursor c = t.order(null);
					while (!c.eof()) {
						long id=c.getInteger("id");
						LogRecord l=cache.get(id); //TODO: cache
						if (l==null) {
							l=LogRecord.create((int) id,sdb);
							fromCursor(c, l);
						}
						if (action.run(l)) break;
						c.next();
					}
					c.close();

				}
			},-1);
		} catch (SqlJetException e) {
			e.printStackTrace();
		}
	}
	private void fromCursor(ISqlJetCursor cur, LogRecord res) throws SqlJetException {
		res.action=cur.getString("action");
		res.date=cur.getString("date");
		res.target=cur.getString("target");
		res.option=cur.getString("option");
	}
	public synchronized LogRecord create() {
		lastNumber++;
		LogRecord res=LogRecord.create(lastNumber,sdb);
		res.date=new Date().toString();
		return res;
	}

	public LogRecord write(String action, String target) {
		LogRecord l=create();
		l.action=action;
		l.target=target;
		save(l);
		return l;
	}
	public void save(final LogRecord log) {
		sdb.reserveWriteTransaction(new DBAction() {
			
			@Override
			public void run(SqlJetDb db) throws SqlJetException {
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
