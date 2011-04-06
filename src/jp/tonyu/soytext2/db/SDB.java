package jp.tonyu.soytext2.db;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import jp.tonyu.soytext2.document.Document;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public class SDB extends SqlJetOpenHelper {
	static final int version=1;
	static final String DOCUMENT_1="document_1";
	static final String LOG_1="log_1";
	
	static final String DOCUMENT_1_LASTUPDATE=DOCUMENT_1+"_lastupdate";
	static final String DOCUMENT_1_LASTACCESSED=DOCUMENT_1+"_lastaccessed";
	static final String DOCUMENT_1_OWNER=DOCUMENT_1+"_owner";
	
	static final String DOCUMENT_CUR_LASTUPDATE = DOCUMENT_1_LASTUPDATE;
	static final String DOCUMENT_CUR=DOCUMENT_1;
	static final String LOG_CUR=LOG_1;

	public SDB(File file) {
		super(file, version);
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
	
	public Iterator<Document> all() {
		
	}
	public Document byId(final String id) throws SqlJetException {
		if (!cache.containsKey(id)) {
			readTransaction(new DBAction() {
				@Override
				public void run(SqlJetDb db) throws SqlJetException {
					ISqlJetTable t = db.getTable(DOCUMENT_CUR);
					ISqlJetCursor cur = t.lookup(null, id);
					if (!cur.eof()) {
						cache.put(id, fromCursor(cur));
					}
				}
			});
		}
		return cache.get(id);
	}
	public Document fromCursor(ISqlJetCursor cur) throws SqlJetException {
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
		writeTransaction(new DBAction() {
			@Override
			public void run(SqlJetDb db) throws SqlJetException {
			    ISqlJetTable t = db.getTable(DOCUMENT_CUR);
			    ISqlJetCursor cur = t.lookup(null, d.id);
			    if (!cur.eof()) {
			    	cur.update(d.id,d.lastUpdate,d.createDate,d.lastAccessed,"javascript",d.summary,d.content,d.owner,d.group,d.permission);
			    } else {
			    	t.insert(d.id,d.lastUpdate,d.createDate,d.lastAccessed,"javascript",d.summary,d.content,d.owner,d.group,d.permission);
			    }				
				cache.put(d.id, d);
			}
		});
	}
}
