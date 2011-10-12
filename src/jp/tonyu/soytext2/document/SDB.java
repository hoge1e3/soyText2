package jp.tonyu.soytext2.document;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import jp.tonyu.db.DBAction;
import jp.tonyu.db.SqlJetHelper;
import jp.tonyu.db.SqlJetRecord;
import jp.tonyu.db.SqlJetRecordCursor;
import jp.tonyu.db.SqlJetTableHelper;
import jp.tonyu.debug.Log;
import jp.tonyu.util.Ref;
import jp.tonyu.util.SFile;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public class SDB extends SqlJetHelper implements DocumentSet {
	static final int version=3;
	/*static final String DOCUMENT_1="document_1";
	static final String LOG_1="log_1";
	
	static final String DOCUMENT_1_LASTUPDATE=DOCUMENT_1+"_lastupdate";
	static final String DOCUMENT_1_LASTACCESSED=DOCUMENT_1+"_lastaccessed";
	static final String DOCUMENT_1_OWNER=DOCUMENT_1+"_owner";
	
	static final String DOCUMENT_CUR_LASTUPDATE = DOCUMENT_1_LASTUPDATE;
	static final String DOCUMENT_CUR=DOCUMENT_1;
	static final String LOG_CUR=LOG_1;
	static final String UID_3="uid";*/
	//static final String UID_3_ID=UID_3+"_id";
	public static final String UID_IMPORT = "77a729a1-5c5d-4d09-9141-72108ee9b634";
	public static final String UID_EXISTENT_FILE = "86e08ee0-0bd5-4d1f-a7f5-66c2251e60ad";
	
	String dbid;
	File blobDir;
	public SDB(File file /*, String uid*/) throws SqlJetException {
		open(file, version);
		blobDir=new File(file.getParentFile(),"blob");
		//this.dbid=uid;
		logManager=new LogManager(this);
		//setupDBID();
		//Log.d(this, "DBID = "+getDBID());
		dbid/*FromFile*/=getDBIDFromFile(new SFile(file.getAbsoluteFile()));
		//Log.d(this, compareDBID());
	}
	/*String dbidFromFile;
	public String compareDBID() {
		return "COMPAREDBID "+dbid+" - "+dbidFromFile;
	}*/
	Pattern dbidPat=Pattern.compile("\\w*\\.\\w+\\.");
	private String getDBIDFromFile(SFile file) {
		try {
			SFile parent=file.parent();
			SFile primaryDBID=parent.rel("primaryDbid.txt");
			if (primaryDBID.exists()) {
				return primaryDBID.text();
			}
			Matcher m=dbidPat.matcher(parent.name());
			if (m.lookingAt()) return parent.name();
			parent=parent.parent();
			m=dbidPat.matcher(parent.name());
			if (m.lookingAt()) return parent.name();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public File getBlobDir() {return blobDir;}
	/*private void setupDBID() throws SqlJetException {
		String gu = getDBID();
		if (gu==null) {
			if (dbid.equals(UID_EXISTENT_FILE)) {
				throw new RuntimeException("Although UID_EXISTENT_FILE, Uid does not set  ");				
			}
			setDBID(dbid);
			return;
		}
		if (gu.equals(dbid)) return;
		if (dbid.equals(UID_EXISTENT_FILE) || dbid.equals(UID_IMPORT)) {
			dbid=gu;
			return;
		}
		throw new RuntimeException(" Uid not match: indb="+gu+"  expect="+dbid);
	}
	public void setDBID(final String dbid) throws SqlJetException {
		writeTransaction(new DBAction() {
			
			@Override
			public void run(SqlJetDb db) throws SqlJetException {
				SqlJetTableHelper t = uidTable();
				ISqlJetCursor cur = t.order(null);
				if (!cur.eof()) {
					cur.delete();
				}
				cur.close();
				t.insert(dbid);
			}
		},-1);
		this.dbid=dbid;
	}
	public String getDBID() throws SqlJetException {
		final Ref<String> res=new Ref<String>();
		readTransaction(new DBAction() {
			@Override
			public void run(SqlJetDb db) throws SqlJetException {
				SqlJetTableHelper t = uidTable();
				ISqlJetCursor cur = t.order(null);
				if (!cur.eof()) {
					res.set(cur.getString("id"));
				}
				cur.close();
			}
		}, -1);
		if (res.isSet()) return res.get();
		return null;
	}*/
	DocumentRecord documentRecord=new DocumentRecord();
	LogRecord logRecord=new LogRecord();
	DBIDRecord dbidRecord=new DBIDRecord();
	
	@Override
	public SqlJetRecord[] tables(int version) {
		return q(documentRecord,logRecord, dbidRecord);
	}
	public String toString() {
		return "(SDB dbid="+dbid+")";
	}
	/*@Override
	protected void onCreate(SqlJetDb db,int version) throws SqlJetException {
		createDocumentTable(db);
		createLogTable(db);		
		createUIDTable(db);
	}
	
	private void createUIDTable(SqlJetDb db) throws SqlJetException {
		db.createTable("CREATE TABLE "+UID_3+" (\n"+
				"     id TEXT NOT NULL PRIMARY KEY\n"+
			    ")\n"+
	    "");
		//db.createIndex("CREATE INDEX "+UID_3_ID+" ON "+UID_3+"(id)");	
		
	}
	private void createLogTable(SqlJetDb db) throws SqlJetException {
		db.createTable("CREATE TABLE "+LOG_1+" (\n"+
	    "   id INTEGER NOT NULL PRIMARY KEY,\n"+
	    "   date TEXT NOT NULL,\n"+
	    "   action TEXT,\n"+
	    "   target TEXT,\n"+
	    "   option TEXT\n"+
	    ")\n"+
	    "");
	}
	private void createDocumentTable(SqlJetDb db) throws SqlJetException {
		db.createTable("CREATE TABLE "+DOCUMENT_1+"(\n"+
			    "   id TEXT NOT NULL PRIMARY KEY,\n"+
			    "   lastupdate INTEGER NOT NULL,\n"+
			    "   createdate INTEGER NOT NULL, \n"+
			    "   lastaccessed INTEGER NOT NULL,\n"+
			    "   language TEXT,\n"+
			    "   summary TEXT,\n"+
			    "   precontent TEXT,\n"+
			    "   content TEXT,\n"+
			    "   owner TEXT,\n"+
			    "   group TEXT,\n"+
			    "   permission TEXT \n"+
			    ")\n"+
			    "");
		db.createIndex("CREATE INDEX "+DOCUMENT_1_LASTUPDATE+" ON "+DOCUMENT_1+"(lastupdate)");
		db.createIndex("CREATE INDEX "+DOCUMENT_1_LASTACCESSED+" ON "+DOCUMENT_1+"(lastaccessed)");
		db.createIndex("CREATE INDEX "+DOCUMENT_1_OWNER+" ON "+DOCUMENT_1+"(owner)");
	}
*/
	Map<String,DocumentRecord> cache=new HashMap<String, DocumentRecord>();
	final private LogManager logManager;
	@Override
	public void all(final DocumentAction action) {
		all(action,true);
	}
	public void all(final DocumentAction action,final boolean fromNewest) {
		try {
			readTransaction(new DBAction() {
				@Override
				public void run(SqlJetDb db) throws SqlJetException {
					//SqlJetTableHelper t = docTable();
					SqlJetRecordCursor<DocumentRecord> cur;
					if (fromNewest) cur=reverseOrder(documentRecord,"lastUpdate");
					else cur = order(documentRecord,"lastUpdate");
					while (!cur.eof()) {
						String id=cur.getString("id");
						if (id!=null) {
							DocumentRecord d=null;
							synchronized (cache) {
								d=cache.get(id);
								if (d==null) {
									d=cur.fetch();
									cache.put(id,d);
								}
							}
							if (action.run(d)) break;
						}
						cur.next();
					}
					cur.close();
				}
			},-1);
		} catch (SqlJetException e) {
			e.printStackTrace();
		}
	}
	@Override
	public DocumentRecord byId(final String id) {
		synchronized (cache) {
			if (!cache.containsKey(id)) {
				try {
					readTransaction(new DBAction() {
						@Override
						public void run(SqlJetDb db) throws SqlJetException {
							DocumentRecord d=find1(documentRecord, null, id);
							if (d!=null) {
								cache.put(id, d);
							}
							/*SqlJetTableHelper t = docTable();
							ISqlJetCursor cur = t.lookup(id);
							if (!cur.eof()) {
								cache.put(id, fromCursor(cur));
							}*/
						}
					},-1);
				} catch (SqlJetException e) {
					e.printStackTrace();
				}
			}
			return cache.get(id);			
		}
	}
	/*private DocumentRecord fromCursor(ISqlJetCursor cur) throws SqlJetException {
    	DocumentRecord d = new DocumentRecord();
    	d.fetch(cur);
    	return d;
	}*/
	@Override
	public void save(final DocumentRecord d) {
	    LogRecord log=logManager.write("save",d.id);
	    d.lastUpdate=log.id;
		reserveWriteTransaction(new DBAction() {
			@Override
			public void run(SqlJetDb db) throws SqlJetException {
			    SqlJetTableHelper t = docTable();
			    ISqlJetCursor cur = t.lookup(null, d.id);
			    Log.d("SAVE", d);
			    //Log.d("SAVE", "Before - "+docCount());
			    if (!cur.eof()) {
			    	d.update(cur);
			    	//cur.update(d.id,d.lastUpdate,d.createDate,d.lastAccessed,"javascript",d.summary,d.preContent,d.content,d.owner,d.group,d.permission);
			    } else {
			    	d.insertTo(t);	
			    	//insertDocument(d, t);
			    }				
			    Log.d("SAVE", d+": done");
			    cur.close();
				cache.put(d.id, d);
			}

		});
	}
	/*private void insertDocument(final DocumentRecord d, ISqlJetTable t) throws SqlJetException {
		t.insert(
				d.id,
				d.lastUpdate,
				d.createDate,
				d.lastAccessed,
				"javascript",
				d.summary,
				d.preContent,
				d.content,
				d.owner,
				d.group,
				d.permission);
	}*/
	public int docCount() throws SqlJetException {
		SqlJetTableHelper t=docTable();
		ISqlJetCursor cur2= t.order();
		int res=(int) cur2.getRowCount();
	    cur2.close();
	    return res;
	}
	/*public ISqlJetTable uidTable() throws SqlJetException {
		return db.getTable(UID_3);
	}
	public ISqlJetTable docTable() throws SqlJetException {
		return db.getTable(DOCUMENT_CUR);
	}
	public ISqlJetTable logTable() throws SqlJetException {
		return db.getTable(LOG_CUR);
	}*/
	public SqlJetTableHelper uidTable() throws SqlJetException {
		return table(dbidRecord);
	}
	public SqlJetTableHelper docTable() throws SqlJetException {
		return table(documentRecord);
	}
	public SqlJetTableHelper logTable() throws SqlJetException {
		return table(logRecord);
	}
	@Override
	public DocumentRecord newDocument() {
		LogRecord log = logManager.write("create","<sameAsThisId>");
		DocumentRecord d=new DocumentRecord();
		d.id=log.id+"@"+dbid;
		d.lastUpdate=log.id;
		d.lastAccessed=log.id;
		cache.put(d.id, d);
		return d;
	}
	@Override
	public DocumentRecord newDocument(String id) {
		if (byId(id)!=null) Log.die(id +" already exists.");
		LogRecord log = logManager.write("create",id);
		DocumentRecord d=new DocumentRecord();
		d.id=id;
		d.lastUpdate=log.id;
		d.lastAccessed=log.id;
		cache.put(d.id, d);
		return d;
	}
	public void printLog () {
		logManager.printAll();
	}
	public void importLog(LogRecord curlog) {
		logManager.importLog(curlog);
	}
	public void all(LogAction action) {
		logManager.all(action);
	}
	@Override
	public int log(String date, String action, String target, String option) {
		LogRecord l=logManager.write(action, target);
		return l.id;
	}
}
