package jp.tonyu.soytext2.document;

import java.util.HashMap;
import java.util.Map;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import jp.tonyu.soytext2.db.DBAction;
import jp.tonyu.soytext2.db.SDB;

public class SLogManager {
	int lastNumber;
	SDB sdb;
	public SLogManager(final SDB sdb) throws SqlJetException {
		super();
		this.sdb=sdb;
		sdb.readTransaction(new DBAction() {
			
			@Override
			public void run(SqlJetDb db) throws SqlJetException {
				ISqlJetTable t = sdb.logTable();
				ISqlJetCursor c = t.order(null);
				lastNumber=0;
				if (c.last()) {
					lastNumber=(int) c.getInteger("id");
				}			
			}
		},-1);
	}
	Map<Integer, SLog> cache=new HashMap<Integer, SLog>(); 
	public SLog byId(final int id) throws SqlJetException {
		if (!cache.containsKey(id)) {
			sdb.readTransaction(new DBAction() {

				@Override
				public void run(SqlJetDb db) throws SqlJetException {
					ISqlJetTable t = sdb.logTable();
					ISqlJetCursor cur = t.lookup(null, id);
					SLog res=new SLog(id);
					if (!cur.eof()) {
						cache.put(id, res);
						res.action=cur.getString("action");
						res.date=cur.getString("date");
						res.target=cur.getString("target");
						res.option=cur.getString("option");
					}

				}
			},-1);
		}
		return cache.get(id);
	}
	public synchronized SLog create() {
		lastNumber++;
		SLog res=new SLog(lastNumber);
		return res;
	}
	public void save(final SLog log) {
		sdb.reserveWriteTransaction(new DBAction() {
			
			@Override
			public void run(SqlJetDb db) throws SqlJetException {
				ISqlJetTable t = sdb.logTable();
				ISqlJetCursor cur = t.lookup(null, log.id);
			    /*
			     * public int id;
	public String date;
	public String action,target,option;
			     */
				if (!cur.eof()) {
			    	cur.update(log.id,log.date,log.action,log.action,log.target,log.option);
			    } else {
			    	t.insert(log.id,log.date,log.action,log.action,log.target,log.option);
			    }
			}
		});
	}
}