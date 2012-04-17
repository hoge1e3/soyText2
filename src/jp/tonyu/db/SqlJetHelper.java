package jp.tonyu.db;

import java.io.File;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jp.tonyu.debug.Log;
import jp.tonyu.util.MapAction;
import jp.tonyu.util.Maps;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.SqlJetDb;
import org.xbill.DNS.Lookup;

/**
 * The helper class which provides some useful routine (transaction, versioning etc.)
 * and simple O-R mapper.
 * @author hoge1e3
 *
 */
public class SqlJetHelper {
	protected SqlJetDb db;
	public SqlJetHelper(){}
	int version;
	/**
	 * Open the SqlJet Database
	 * @param file
	 * @param version
	 * @throws SqlJetException
	 */
	public void open(File file, final int version) throws SqlJetException {
		if (version<=0) {
			throw new RuntimeException("Version must be >0");
		}
		db=SqlJetDb.open(file, true);
		writeTransaction(new DBAction() {
			@Override
			public void run(SqlJetDb db) throws SqlJetException {
				int dbVer=db.getOptions().getUserVersion();
				if (dbVer==0) {
					create(db,version);
				} else if (dbVer!=version) {
					upgrade(dbVer,version);
				}
				SqlJetHelper.this.version=version;
			}
		},0);

		reservedTransactionThread.start();
	}
	int readCount=0;
	//SqlJetTransactionMode transaction=null;
	public void writeTransaction(DBAction action, int timeOut) throws SqlJetException {
		try {
			waitForTransaction(SqlJetTransactionMode.WRITE, timeOut);
			action.run(db);
			commit();
			action.afterCommit(db);
		} catch(SqlJetException e) {
			e.printStackTrace();
			rollback();
			action.afterRollback(db);
		}
	}
	final List<DBAction> reservedWriteTransaction= new Vector<DBAction>();
	public void reserveWriteTransaction(DBAction action) {
		reservedWriteTransaction.add(action);
	}
	public void waitForTransaction(SqlJetTransactionMode mode,int timeOut) throws SqlJetException  {
		if (timeOut<0) timeOut=-1;
		else timeOut=timeOut/100;
		while (!beginTransaction(mode)) {
			Log.d(this,"Wait for trans "+mode);
			try {
				Thread.sleep(100);
				if (timeOut==0) throw new SqlJetException("waitForTransaction: Timed out ");
				timeOut--;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	/*public void joinReadTransaction(DBAction action) throws SqlJetException {
		if (transaction==SqlJetTransactionMode.READ_ONLY) {
			readCount++;
			action.run(db);
			readCount--;
			if (readCount==0) commit();

		}
		else {
			readTransaction(action);
		}
	}*/
	SqlJetTransactionMode mode;
	public synchronized boolean beginTransaction(SqlJetTransactionMode mode) throws SqlJetException {
		if (this.mode==null) {
			this.mode=mode;
			db.beginTransaction(mode);
			if (mode==SqlJetTransactionMode.READ_ONLY) readCount++;
			return true;
		}
		if (this.mode==mode && mode==SqlJetTransactionMode.READ_ONLY) {
			readCount++;
			return true;
		}
		return false;
	}

	public void readTransaction(DBAction action, int timeOut) throws SqlJetException {
		try {
			waitForTransaction(SqlJetTransactionMode.READ_ONLY, timeOut);
			action.run(db);
		} finally {
			commit();
			action.afterCommit(db);
		}
	}
	final Thread reservedTransactionThread=new Thread() {
		@Override
		public void run() {
			while(true) {
				while(reservedWriteTransaction.size()>0) {
					try {
						writeTransaction(  reservedWriteTransaction.remove(0),-1 );
					} catch (SqlJetException e) {
						e.printStackTrace();
					}
				}
				if (closing && reservedWriteTransaction.size()==0) break;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};
	private synchronized void rollback() throws SqlJetException {
		if (mode==SqlJetTransactionMode.READ_ONLY) {
			readCount--;
			if (readCount>0) return;
		}
		db.rollback();
		mode=null;
	}
	public synchronized void commit() throws SqlJetException {
		if (mode==SqlJetTransactionMode.READ_ONLY) {
			readCount--;
			if (readCount>0) return;
		}
		db.commit();
		mode=null;
	}
	private void create(SqlJetDb db,int newVersion) throws SqlJetException {
		onCreate(db,newVersion);
		db.getOptions().setUserVersion(newVersion);
	}
	private void upgrade(int oldVersion, int newVersion) throws SqlJetException {
		onUpgrade(db,oldVersion,newVersion);
		db.getOptions().setUserVersion(newVersion);
	}
	protected void onUpgrade(SqlJetDb db, int oldVersion, int newVersion) throws SqlJetException {
		//System.out.println("Version "+oldVersion+" -> "+newVersion);
	}

	protected void onCreate(final SqlJetDb db, int version) throws SqlJetException {
		//System.out.println("Created");
		for (SqlJetRecord r: tables(version)) {
			try {
				r.createTableAndIndex(SqlJetHelper.this);
			} catch (NoSuchFieldException e) {
				//e.printStackTrace();
				throw new SqlJetException(e);
			}
		}
	}
	/*public static void main(String[] args) throws SqlJetException {
		new SqlJetHelper(new File("empty.db"), 3);
	}*/
	boolean closing=false;
	public void close() throws SqlJetException {
		Log.d(this,"Closing..");
		closing=true;

		try {
			reservedTransactionThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		db.close();
		Log.d(this,"Closed");
	}
	public SqlJetTableHelper table(String name) {
		return new SqlJetTableHelper(db, name);
	}
	/*public static String tableName(Class klass) {
		return klass.getName().replaceAll("\\.", "_");
	}*/
	public SqlJetTableHelper table(SqlJetRecord r) {
		return table(r.tableName());
	}
	public SqlJetRecord[] tables(int version) {
		return q();
	}
	public static <T> T[] q(T...ts) {
		return ts;
	}

	public <T extends SqlJetRecord> T find1(T record, String attrNames, Object... values) throws SqlJetException {
		SqlJetTableHelper t = table(record);
		ISqlJetCursor cur = t.lookup(attrNames, values);
		T res=null;
		if (!cur.eof()) {
			res = record.dup(record);
			res.fetch(cur);
		}
		cur.close();
		return res;
	}
	public <T extends SqlJetRecord> SqlJetRecordCursor<T> order(T record, String attrNames) throws SqlJetException {
		SqlJetTableHelper t = table(record);
		ISqlJetCursor cur = t.order(attrNames);
		return new SqlJetRecordCursor<T>(record, cur);
	}
	public <T extends SqlJetRecord> SqlJetRecordCursor<T> reverseOrder(T record, String attrNames) throws SqlJetException {
		SqlJetTableHelper t = table(record);
		ISqlJetCursor cur = t.order(attrNames);
		return new SqlJetRecordCursor<T>(record, cur.reverse());
	}
	public Map<String, List<Map<String,Object>>> backup() throws SqlJetException {
		final Map<String, List<Map<String,Object>>> res=new HashMap<String, List<Map<String,Object>>>();
		readTransaction(new DBAction() {

			@Override
			public void run(SqlJetDb db) throws SqlJetException {
				for (SqlJetRecord r:tables(version)) {
					if (!table(r).exists()) continue;
					SqlJetRecordCursor<SqlJetRecord> cur = order(r,null);
					List<Map<String,Object>> list=new Vector<Map<String,Object>>();
					res.put(r.tableName(), list);
					while (!cur.eof()) {
						SqlJetRecord re = cur.fetch();
						try {
							Log.d("Export", re);
							list.add(re.toMap());
						} catch (Exception e) {
							e.printStackTrace();
							throw new SqlJetException(e);
						}
						cur.next();
					}
					cur.close();
				}
			}
		}, -1);
		return res;
	}
	public Map<String, SqlJetRecord> tablesAsMap(int version) {
		Map<String, SqlJetRecord> res=new HashMap<String, SqlJetRecord>();
		for (SqlJetRecord r:tables(version)) {
			res.put(r.tableName(), r);
		}
		return res;
	}
	//                            table  record    field  value
	public void restore(final Map<String, List<Map<String,Object>>> data) throws SqlJetException {
		final Map<String, SqlJetRecord> tables=tablesAsMap(version);
		writeTransaction(new DBAction() {

			@Override
			public void run(SqlJetDb db) throws SqlJetException {
				for (Map.Entry<String, List<Map<String,Object>>> e:data.entrySet()) {
					String key=e.getKey();
					List<Map<String,Object>> value=e.getValue();
					SqlJetRecord r = tables.get(key);
					if (r==null) return;
					SqlJetTableHelper t = table(r.tableName());
					ISqlJetCursor cur = t.order();
					while (!cur.eof()) {
						cur.delete();
						cur.next();
					}
					cur.close();
					for (Map<String,Object> m:value) {
						r.copyFrom(m);
						r.insertTo(t);
					}
				}
			}
		}, -1);
	}
}
