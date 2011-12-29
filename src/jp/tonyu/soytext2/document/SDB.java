package jp.tonyu.soytext2.document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import jp.tonyu.db.DBAction;
import jp.tonyu.db.SqlJetHelper;
import jp.tonyu.db.SqlJetRecord;
import jp.tonyu.db.SqlJetRecordCursor;
import jp.tonyu.db.SqlJetTableHelper;
import jp.tonyu.debug.Log;
import jp.tonyu.util.MapAction;
import jp.tonyu.util.Maps;
import jp.tonyu.util.Ref;
import jp.tonyu.util.SFile;
import jp.tonyu.util.TDate;

import net.arnx.jsonic.JSON;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public class SDB extends SqlJetHelper implements DocumentSet {
	public static final String PRIMARY_DBID_TXT = "primaryDbid.txt";

	static final int version=3;
	//public static final String UID_IMPORT = "77a729a1-5c5d-4d09-9141-72108ee9b634";
	//public static final String UID_EXISTENT_FILE = "86e08ee0-0bd5-4d1f-a7f5-66c2251e60ad";
	
	String dbid;
	final File dbFile;
	SFile blobDir;
	SFile backupDir;
	SFile homeDir;
	public SDB(File file /*, String uid*/) throws SqlJetException {
		open(file, version);
		dbFile=file;
		homeDir=new SFile(file).parent();
		blobDir=homeDir.rel("blob");
		backupDir=homeDir.rel("backup");
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
			SFile primaryDBID=parent.rel(PRIMARY_DBID_TXT);
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
	public SFile getBlobDir() {return blobDir;}
	
	DocumentRecord documentRecord=new DocumentRecord();
	LogRecord logRecord=new LogRecord();
	//DBIDRecord dbidRecord=new DBIDRecord();
	IndexRecord indexRecord=new IndexRecord();
	
	@Override
	public SqlJetRecord[] tables(int version) {
		return q(documentRecord,logRecord);//, dbidRecord/*,indexRecord*/);
	}
	public String toString() {
		return "(SDB dbid="+dbid+")";
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
					//SqlJetTableHelper t = docTable();
					SqlJetRecordCursor<DocumentRecord> cur;
					if (fromNewest) cur=reverseOrder(documentRecord,"lastUpdate");
					else cur = order(documentRecord,"lastUpdate");
					while (!cur.eof()) {
						String id=cur.getString("id");
						if (id!=null) {
							DocumentRecord d = fetchDocumentFromCursorOrCache(cur, id);
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
			DocumentRecord res = cache.get(id);
			//if (res.content==null) Log.die(res.id+" has null content");
			return res;			
		}
	}
	/*public void save(DocumentRecord d) {
		save(d,new HashMap<String,String>());
	}*/
	@Override
	public void save(final DocumentRecord d, final Map<String, String> indexValues) {
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
				updateIndexInTransaction(d, indexValues);
			}
		});
	}
	public int docCount() throws SqlJetException {
		SqlJetTableHelper t=docTable();
		ISqlJetCursor cur2= t.order();
		int res=(int) cur2.getRowCount();
	    cur2.close();
	    return res;
	}
	/*public SqlJetTableHelper uidTable() throws SqlJetException {
		return table(dbidRecord);
	}*/
	public SqlJetTableHelper docTable() throws SqlJetException {
		return table(documentRecord);
	}
	public SqlJetTableHelper logTable() throws SqlJetException {
		return table(logRecord);
	}
	public SqlJetTableHelper indexTable() {
		return table(indexRecord);
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
	private void addIndexValue(DocumentRecord d,String name, String value) throws SqlJetException {
		// use in writetransaction
		IndexRecord i = new IndexRecord();
		i.document=d.id;
		i.name=name;
		i.value=value;
		i.lastUpdate=d.lastUpdate;
		i.insertTo(indexTable());
	}
	private void removeIndexValues(final DocumentRecord d) throws SqlJetException {
		// use in writetransaction
		ISqlJetCursor cur= indexTable().scope("document", d.id, d.id);
		while (!cur.eof()) {
			cur.delete();
			cur.next();
		}
		cur.close();
	}
	public void byIndex(final String name,final String value,final DocumentAction action) throws SqlJetException {
		readTransaction(new DBAction() {
			
			@Override
			public void run(SqlJetDb db) throws SqlJetException {
				ISqlJetCursor _cur = indexTable().scope("name,value,lastUpdate", 
						q(name,value,Long.MIN_VALUE), q(name,value,Long.MAX_VALUE)).reverse();
				SqlJetRecordCursor<DocumentRecord> cur=new SqlJetRecordCursor<DocumentRecord>(documentRecord,_cur);
				while (!cur.eof()) {
					String id=cur.getString("document");
					if (id!=null) {
						DocumentRecord d = fetchDocumentFromCursorOrCache(cur, id);
						if (action.run(d)) break;
					}
					cur.next();
				}
				cur.close();
			}
		},-1);
	}
	/*public void resetIndex() throws SqlJetException {
		writeTransaction(new DBAction() {
			
			@Override
			public void run(SqlJetDb db) throws SqlJetException {
				ISqlJetCursor cur = indexTable().order();
				while (!cur.eof()) {
					cur.delete();
					cur.next();
				}
				cur.close();
				cur = docTable().order();
				while (!cur.eof()) {
					documentRecord.fetch(cur);
					
				}
			}
		}, -1);
	}*/
	private DocumentRecord fetchDocumentFromCursorOrCache(
			SqlJetRecordCursor<DocumentRecord> cur, String id)
			throws SqlJetException {
		DocumentRecord d=null;
		synchronized (cache) {
			d=cache.get(id);
			if (d==null) {
				d=cur.fetch();
				cache.put(id,d);
			}
		}
		return d;
	}
	@Override
	public void updateIndex(final DocumentRecord d,
			final Map<String, String> indexValues) {
		reserveWriteTransaction(new DBAction() {
			@Override
			public void run(SqlJetDb db) throws SqlJetException {
				updateIndexInTransaction(d, indexValues);
			}
		});
	}
	private void updateIndexInTransaction(final DocumentRecord d,
			final Map<String, String> indexValues) throws SqlJetException {
		if (IndexRecord.useIndex) {
			removeIndexValues(d);
			for (Map.Entry<String, String> e:indexValues.entrySet()) {
				addIndexValue(d, e.getKey(), e.getValue());
			}
		}
	}
	public static final String MIN_STRING="",MAX_STRING=new String(new char[]{65535,65535,65535});
	/*
	public void addIndexName(final String name) throws SqlJetException {
		Set<String> s = indexNames();
		if (s.contains(name)) return;
		reserveWriteTransaction(new DBAction() {
			
			@Override
			public void run(SqlJetDb db) throws SqlJetException {
				IndexRecord i = new IndexRecord();
				i.name= IndexRecord.DEFINED_INDEX_NAMES;
				i.value=name;
				i.insertTo(indexTable());
				indexNames.add(name);
			}
		});
	}
	HashSet<String> indexNames = new HashSet<String>();

	public Set<String> indexNames() throws SqlJetException {
		if (indexNames!=null) return indexNames;
		indexNames = new HashSet<String>();
		readTransaction(new DBAction() {
			
			@Override
			public void run(SqlJetDb db) throws SqlJetException {
				ISqlJetCursor cur = indexTable().scope("name,value,lastUpdate", 
						new Object[]{IndexRecord.DEFINED_INDEX_NAMES, MIN_STRING},
						new Object[]{IndexRecord.DEFINED_INDEX_NAMES, MAX_STRING});
				IndexRecord rec=new IndexRecord();
				while (!cur.eof()) {
					rec.fetch(cur);
					indexNames.add(rec.value);
					cur.next();
				}
				cur.close();
			}
		}, -1);
		return indexNames;
	}*/
	public SFile newestBackupFile() {
		SFile src=null;
		for (SFile txt:backupDir) {
			if (!txt.name().endsWith(".json")) continue;
			if (src==null || txt.lastModified()>src.lastModified()) {
				src=txt;
			}
		}
		return src;
	}
	public SFile newBackupFile() {
		String d=new TDate().toString("yyyy_MMdd_hh_mm_ss");
		return backupDir.rel("main.db."+d+".json");
	}
	public void backupToFile() throws SqlJetException, IOException {
		Object b=backup();
		JSON json = new JSON();
		json.setPrettyPrint(true);
		OutputStream out = newBackupFile().outputStream();
		json.format(b, out);
		out.close();
	}
	public void restoreFromNewestFile() throws IOException, SqlJetException {
		SFile src=newestBackupFile();
		InputStream in = src.inputStream();
		Map b=(Map)JSON.decode(in);
		in.close();
		restore(b);
	}
	public void cloneWithFilter(SDB dest, String[] ids) {
		for (String id:ids) {
			DocumentRecord d=byId(id);
			dest.save(d, new HashMap<String,String>());
		}
		dest.logManager.setLastNumber(logManager.lastNumber);
	}
	public File getFile() {
		return dbFile;
	}
	@Override
	public String getDBID() {
		return dbid;
	}
	
}
