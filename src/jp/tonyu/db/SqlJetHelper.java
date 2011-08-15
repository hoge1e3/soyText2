package jp.tonyu.db;

import java.io.File;
import java.util.List;
import java.util.Vector;

import jp.tonyu.debug.Log;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public class SqlJetHelper {
	protected final SqlJetDb db;
	public SqlJetHelper(File file, final int version) throws SqlJetException {
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
	private synchronized void commit() throws SqlJetException {
		if (mode==SqlJetTransactionMode.READ_ONLY) {
			readCount--;
			if (readCount>0) return;
		}
		db.commit();
		mode=null;
	}
	private void create(SqlJetDb db,int newVersion) throws SqlJetException {
		onCreate(db);
		db.getOptions().setUserVersion(newVersion);
	}
	private void upgrade(int oldVersion, int newVersion) throws SqlJetException {
		onUpgrade(db,oldVersion,newVersion);
		db.getOptions().setUserVersion(newVersion);
	}
	protected void onUpgrade(SqlJetDb db, int oldVersion, int newVersion) throws SqlJetException {
		//System.out.println("Version "+oldVersion+" -> "+newVersion);
	}
	
	protected void onCreate(SqlJetDb db) throws SqlJetException {
		//System.out.println("Created");
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
}
