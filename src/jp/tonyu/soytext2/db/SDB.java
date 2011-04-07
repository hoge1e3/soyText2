package jp.tonyu.soytext2.db;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogManager;

import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.document.Document;
import jp.tonyu.soytext2.document.DocumentAction;
import jp.tonyu.soytext2.document.SLog;
import jp.tonyu.soytext2.document.SLogManager;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public class SDB extends SqlJetHelper {
	static final int version=1;
	static final String DOCUMENT_1="document_1";
	static final String LOG_1="log_1";
	
	static final String DOCUMENT_1_LASTUPDATE=DOCUMENT_1+"_lastupdate";
	static final String DOCUMENT_1_LASTACCESSED=DOCUMENT_1+"_lastaccessed";
	static final String DOCUMENT_1_OWNER=DOCUMENT_1+"_owner";
	
	static final String DOCUMENT_CUR_LASTUPDATE = DOCUMENT_1_LASTUPDATE;
	static final String DOCUMENT_CUR=DOCUMENT_1;
	static final String LOG_CUR=LOG_1;

	public SDB(File file) throws SqlJetException {
		super(file, version);
		logManager=new SLogManager(this);
	}
	@Override
	protected void onCreate(SqlJetDb db) throws SqlJetException {
		db.createTable("CREATE TABLE "+DOCUMENT_1+"(\n"+
			    "   id TEXT NOT NULL PRIMARY KEY,\n"+
			    "   lastupdate INTEGER NOT NULL,\n"+
			    "   createdate INTEGER NOT NULL, \n"+
			    "   lastaccessed INTEGER NOT NULL,\n"+
			    "   language TEXT,\n"+
			    "   summary TEXT,\n"+
			    "   content TEXT,\n"+
			    "   owner TEXT,\n"+
			    "   group TEXT,\n"+
			    "   permission TEXT \n"+
			    ")\n"+
			    "");
		db.createTable("CREATE TABLE "+LOG_1+" (\n"+
	    "   id INTEGER NOT NULL PRIMARY KEY,\n"+
	    "   date TEXT NOT NULL,\n"+
	    "   action TEXT,\n"+
	    "   target TEXT,\n"+
	    "   option TEXT\n"+
	    ")\n"+
	    "");
		//db.createIndex("CREATE INDEX document_id ON document(id)");
		db.createIndex("CREATE INDEX "+DOCUMENT_1_LASTUPDATE+" ON "+DOCUMENT_1+"(lastupdate)");
		db.createIndex("CREATE INDEX "+DOCUMENT_1_LASTACCESSED+" ON "+DOCUMENT_1+"(lastaccessed)");
		db.createIndex("CREATE INDEX "+DOCUMENT_1_OWNER+" ON "+DOCUMENT_1+"(owner)");
		
	}
	Map<String,Document> cache=new HashMap<String, Document>();
	private SLogManager logManager;
	
	public void all(final DocumentAction action) throws SqlJetException {
		readTransaction(new DBAction() {
			
			@Override
			public void run(SqlJetDb db) throws SqlJetException {
				ISqlJetTable t = docTable();
				//ISqlJetCursor cur = t.order(DOCUMENT_CUR_LASTUPDATE);
				//ISqlJetCursor cur = t.scope(DOCUMENT_CUR_LASTUPDATE, new Object[]{24}, new Object[]{36});
			    
				
				ISqlJetCursor cur = t.order(DOCUMENT_CUR_LASTUPDATE); //, new Object[]{null}, new Object[]{null});
				//Log.d("ALL", "Disp - "+cur.getRowCount());
				cur=cur.reverse();
				while (!cur.eof()) {
					String id=cur.getString("id");
					Document d=null;
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
	}
	public Document byId(final String id) throws SqlJetException {
		synchronized (cache) {
			if (!cache.containsKey(id)) {
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
			}
			return cache.get(id);			
		}
	}
	private Document fromCursor(ISqlJetCursor cur) throws SqlJetException {
    	Document d = new Document();
		d.id=cur.getString("id");
    	d.lastUpdate=cur.getInteger("lastupdate");
    	d.createDate=cur.getInteger("createdate");
    	d.lastAccessed=cur.getInteger("lastaccessed");
    	d.summary=cur.getString("summary");
    	d.content=cur.getString("content");
    	d.owner=cur.getString("owner");
    	d.group=cur.getString("group");
    	d.permission=cur.getString("permission");
    	return d;
	}
	public void save(final Document d) throws SqlJetException {
		reserveWriteTransaction(new DBAction() {
			@Override
			public void run(SqlJetDb db) throws SqlJetException {
			    ISqlJetTable t = docTable();
			    ISqlJetCursor cur = t.lookup(null, d.id);
			    SLog log=logManager.write("save",d.id);
			    d.lastUpdate=log.id;
			    Log.d("SAVE", d);
			    //Log.d("SAVE", "Before - "+docCount());
			    if (!cur.eof()) {
			    	cur.update(d.id,d.lastUpdate,d.createDate,d.lastAccessed,"javascript",d.summary,d.content,d.owner,d.group,d.permission);
			    } else {
			    	t.insert(d.id,d.lastUpdate,d.createDate,d.lastAccessed,"javascript",d.summary,d.content,d.owner,d.group,d.permission);
			    }				
			    Log.d("SAVE", d+": done");
			    cur.close();
				cache.put(d.id, d);
			}
		});
	}
	public int docCount() throws SqlJetException {
		ISqlJetTable t=docTable();
		ISqlJetCursor cur2= t.order(null);
		int res=(int) cur2.getRowCount();
	    cur2.close();
	    return res;
	}
	public ISqlJetTable docTable() throws SqlJetException {
		return db.getTable(DOCUMENT_CUR);
	}
	public ISqlJetTable logTable() throws SqlJetException {
		return db.getTable(LOG_CUR);
	}
	public Document newDocument() throws SqlJetException {
		Document d=new Document();
		SLog log = logManager.write("create","<sameAsThisId>");
		d.id=log.id+"";
		d.lastUpdate=log.id;
		d.lastAccessed=log.id;
		return d;
	}
	public void printLog () {
		logManager.printAll();
	}
	
}
