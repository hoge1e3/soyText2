package jp.tonyu.soytext2.document;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import jp.tonyu.db.DBAction;
import jp.tonyu.db.SqlJetHelper;
import jp.tonyu.debug.Log;
import jp.tonyu.util.Ref;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public class SDB extends SqlJetHelper implements DocumentSet {
	static final int version=3;
	static final String DOCUMENT_1="document_1";
	static final String LOG_1="log_1";
	
	static final String DOCUMENT_1_LASTUPDATE=DOCUMENT_1+"_lastupdate";
	static final String DOCUMENT_1_LASTACCESSED=DOCUMENT_1+"_lastaccessed";
	static final String DOCUMENT_1_OWNER=DOCUMENT_1+"_owner";
	
	static final String DOCUMENT_CUR_LASTUPDATE = DOCUMENT_1_LASTUPDATE;
	static final String DOCUMENT_CUR=DOCUMENT_1;
	static final String LOG_CUR=LOG_1;
	static final String UID_3="uid";
	//static final String UID_3_ID=UID_3+"_id";
	public static final String UID_IMPORT = "77a729a1-5c5d-4d09-9141-72108ee9b634";
	public static final String UID_EXISTENT_FILE = "86e08ee0-0bd5-4d1f-a7f5-66c2251e60ad";
	
	String uid;
	public SDB(File file , String uid) throws SqlJetException {
		super(file, version);
		this.uid=uid;
		logManager=new LogManager(this);
		//setupUID();
	}
	private void setupUID() throws SqlJetException {
		String gu = getUID();
		if (gu==null) {
			setUID(uid);
			return;
		}
		if (gu.equals(uid)) return;
		if (gu.equals(UID_EXISTENT_FILE) || gu.equals(UID_IMPORT)) {
			uid=gu;
		}
		throw new RuntimeException(" Uid not match: indb="+gu+"  expect="+uid);
	}
	public void setUID(final String uid) throws SqlJetException {
		writeTransaction(new DBAction() {
			
			@Override
			public void run(SqlJetDb db) throws SqlJetException {
				ISqlJetTable t = uidTable();
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
				ISqlJetTable t = uidTable();
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
	@Override
	protected void onCreate(SqlJetDb db) throws SqlJetException {
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
					ISqlJetTable t = docTable();
					ISqlJetCursor cur = t.order(DOCUMENT_CUR_LASTUPDATE);
					if (fromNewest) cur=cur.reverse();
					while (!cur.eof()) {
						String id=cur.getString("id");
						DocumentRecord d=null;
						synchronized (cache) {
							d=cache.get(id);
							if (d==null) {
								d=fromCursor(cur);
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
							ISqlJetTable t = docTable();
							ISqlJetCursor cur = t.lookup(null, id);
							if (!cur.eof()) {
								cache.put(id, fromCursor(cur));
							}
						}
					},-1);
				} catch (SqlJetException e) {
					e.printStackTrace();
				}
			}
			return cache.get(id);			
		}
	}
	private DocumentRecord fromCursor(ISqlJetCursor cur) throws SqlJetException {
    	DocumentRecord d = new DocumentRecord(this, cur.getString("id"));
    	d.lastUpdate=cur.getInteger("lastupdate");
    	d.createDate=cur.getInteger("createdate");
    	d.lastAccessed=cur.getInteger("lastaccessed");
    	d.summary=cur.getString("summary");
    	d.preContent=cur.getString("precontent");
    	d.content=cur.getString("content");
    	d.owner.set(cur.getString("owner"));
    	d.group.set(cur.getString("group"));
    	d.permission.set(cur.getString("permission"));
    	return d;
	}
	@Override
	public void save(final DocumentRecord d) {
		reserveWriteTransaction(new DBAction() {
			@Override
			public void run(SqlJetDb db) throws SqlJetException {
			    ISqlJetTable t = docTable();
			    ISqlJetCursor cur = t.lookup(null, d.id);
			    LogRecord log=logManager.write("save",d.id);
			    d.lastUpdate=log.id;
			    Log.d("SAVE", d);
			    //Log.d("SAVE", "Before - "+docCount());
			    if (!cur.eof()) {
			    	cur.update(d.id,d.lastUpdate,d.createDate,d.lastAccessed,"javascript",d.summary,d.preContent,d.content,d.owner.get(),d.group.get(),d.permission.get());
			    } else {
			    	insertDocument(d, t);
			    }				
			    Log.d("SAVE", d+": done");
			    cur.close();
				cache.put(d.id, d);
			}

		});
	}
	private void insertDocument(final DocumentRecord d, ISqlJetTable t) throws SqlJetException {
		t.insert(
				d.id,
				d.lastUpdate,
				d.createDate,
				d.lastAccessed,
				"javascript",
				d.summary,
				d.preContent,
				d.content,
				d.owner.get(),
				d.group.get(),
				d.permission.get());
	}
	public int docCount() throws SqlJetException {
		ISqlJetTable t=docTable();
		ISqlJetCursor cur2= t.order(null);
		int res=(int) cur2.getRowCount();
	    cur2.close();
	    return res;
	}
	public ISqlJetTable uidTable() throws SqlJetException {
		return db.getTable(UID_3);
	}
	public ISqlJetTable docTable() throws SqlJetException {
		return db.getTable(DOCUMENT_CUR);
	}
	public ISqlJetTable logTable() throws SqlJetException {
		return db.getTable(LOG_CUR);
	}
	@Override
	public DocumentRecord newDocument() {
		LogRecord log = logManager.write("create","<sameAsThisId>");
		DocumentRecord d=new DocumentRecord(this, log.id+"");
		d.lastUpdate=log.id;
		d.lastAccessed=log.id;
		cache.put(d.id, d);
		return d;
	}
	@Override
	public DocumentRecord newDocument(String id) {
		if (byId(id)!=null) return null;
		LogRecord log = logManager.write("create",id);
		DocumentRecord d=new DocumentRecord(this, id);
		d.lastUpdate=log.id;
		d.lastAccessed=log.id;
		cache.put(d.id, d);
		return d;
	}
	public void printLog () {
		logManager.printAll();
	}
	// migration - error CORUPPT
	@Override
	protected void onUpgrade(SqlJetDb db, int oldVersion, int newVersion)
			throws SqlJetException {
		if (oldVersion==1 && newVersion==2) {
			ISqlJetTable t = docTable();
			ISqlJetCursor cur = t.order(DOCUMENT_CUR_LASTUPDATE);
			Vector<DocumentRecord> recs=new Vector<DocumentRecord>();
			while (!cur.eof()) {
				DocumentRecord d=null;
				d=fromCursor_ver1(cur);
				recs.add(d);
				cur.next();
			}
			cur.close();
			dropDocumentTable1(db);
			createDocumentTable(db);
			t = docTable();
			for (DocumentRecord d:recs) {
				System.out.println(d);
				if (d.id.equals("100309_210119")) {
					Object[] r=new Object[]{				d.id,
				d.lastUpdate,
				d.createDate,
				d.lastAccessed,
				"javascript",
				d.summary,
				d.preContent,
				d.content,
				d.owner.get(),
				d.group.get(),
				d.permission.get()};
					for (Object rr:r) {
						System.out.println(rr);
					}
					continue;
				}
				insertDocument(d,t);
			}
		}
	}
	private void dropDocumentTable1(SqlJetDb db) throws SqlJetException {
		db.dropIndex(DOCUMENT_1_LASTACCESSED);
		db.dropIndex(DOCUMENT_1_LASTUPDATE);
		db.dropIndex(DOCUMENT_1_OWNER);
		db.dropTable(DOCUMENT_1);
	}
	private DocumentRecord fromCursor_ver1(ISqlJetCursor cur) throws SqlJetException {
    	DocumentRecord d = new DocumentRecord(this, cur.getString("id"));
    	d.lastUpdate=cur.getInteger("lastupdate");
    	d.createDate=cur.getInteger("createdate");
    	d.lastAccessed=cur.getInteger("lastaccessed");
    	d.summary=cur.getString("summary");
    	d.preContent="$.create();";
    	d.content=cur.getString("content");
    	d.owner.set(cur.getString("owner"));
    	d.group.set(cur.getString("group"));
    	d.permission.set(cur.getString("permission"));
    	return d;
	}
	public void importLog(LogRecord curlog) {
		logManager.importLog(curlog);
	}
	public void all(LogAction action) {
		logManager.all(action);
	}

}
