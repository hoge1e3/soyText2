package jp.tonyu.soytext2.db;

import java.io.File;
import java.util.List;
import java.util.Queue;
import java.util.Vector;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public class SqlJetOpenHelper {
	SqlJetDb db;
	public SqlJetOpenHelper(File file, final int version) {
		if (version<=0) {
			throw new RuntimeException("Version must be >0");
		}
		try {
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
			});			
		} catch (SqlJetException e) {
			e.printStackTrace();
		}
		
	}
	SqlJetTransactionMode transaction=null;
	public synchronized void writeTransaction(DBAction action) throws SqlJetException {
		db.beginTransaction(transaction=SqlJetTransactionMode.WRITE);
		action.run(db);
		commit();
	}
	List<DBAction> reserved= new Vector<DBAction>();
	public synchronized void reserveWriteTransaction(DBAction action) throws SqlJetException {
		if (transaction==null) writeTransaction(action);
		else reserved.add(action); 
	}
	public synchronized void readTransaction(DBAction action) throws SqlJetException {
		db.beginTransaction(transaction=SqlJetTransactionMode.READ_ONLY);
		action.run(db);
		commit();
	}
	private void commit() throws SqlJetException {
		db.commit();
		transaction=null;
		while(reserved.size()>0) {
			reserved.remove(0).run(db);
		}
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
	public static void main(String[] args) {
		new SqlJetOpenHelper(new File("empty.db"), 3);
	}
}
