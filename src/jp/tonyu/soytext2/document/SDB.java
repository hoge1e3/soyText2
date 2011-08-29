package jp.tonyu.soytext2.document;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


import jp.tonyu.db.DBAction;
import jp.tonyu.db.SqlJetHelper;
import jp.tonyu.db.SqlJetRecord;
import jp.tonyu.db.SqlJetRecordCursor;
import jp.tonyu.db.SqlJetTableHelper;
import jp.tonyu.debug.Log;
import jp.tonyu.util.Ref;

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
	
	String uid;
	public SDB(File file , String uid) throws SqlJetException {
		super(file, version);
		this.uid=uid;
		logManager=new LogManager(this);
		setupUID();
		Log.d(this, "UID = "+getUID());
	}
	private void setupUID() throws SqlJetException {
		String gu = getUID();
		if (gu==null) {
			if (uid.equals(UID_EXISTENT_FILE)) {
				throw new RuntimeException("Although UID_EXISTENT_FILE, Uid does not set  ");				
			}
			setUID(uid);
			return;
		}
		if (gu.equals(uid)) return;
		if (uid.equals(UID_EXISTENT_FILE) || uid.equals(UID_IMPORT)) {
			uid=gu;
			return;
		}
		throw new RuntimeException(" Uid not match: indb="+gu+"  expect="+uid);
	}
	public void setUID(final String uid) throws SqlJetException {
		writeTransaction(new DBAction() {
			
			@Override
			public void run(SqlJetDb db) throws SqlJetException {
				SqlJetTableHelper t = uidTable();
				ISqlJetCursor cur = t.order(null);
				if (!cur.eof()) {
					cur.delete();
				}
				cur.close();
				t.insert(uid);
			}
		},-1);
		this.uid=uid;
	}
	public String getUID() throws SqlJetException {
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
	}
	DocumentRecord documentRecord=new DocumentRecord();
	LogRecord logRecord=new LogRecord();
	UIDRecord uidRecord=new UIDRecord();
	@Override
	public SqlJetRecord[] tables(int version) {
		return q(documentRecord,logRecord, uidRecord);
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
						DocumentRecord d=null;
						synchronized (cache) {
							d=cache.get(id);
							if (d==null) {
								d=cur.fetch();
								cache.put(id,d);
							}
						}
						if (action.run(d)) break;
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
		reserveWriteTransaction(new DBAction() {
			@Override
			public void run(SqlJetDb db) throws SqlJetException {
			    SqlJetTableHelper t = docTable();
			    ISqlJetCursor cur = t.lookup(null, d.id);
			    LogRecord log=logManager.write("save",d.id);
			    d.lastUpdate=log.id;
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
		return table(uidRecord);
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
		d.id=log.id+"@"+uid;
		d.lastUpdate=log.id;
		d.lastAccessed=log.id;
		cache.put(d.id, d);
		return d;
	}
	@Override
	public DocumentRecord newDocument(String id) {
		if (byId(id)!=null) return null;
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

}
