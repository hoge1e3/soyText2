package jp.tonyu.soytext2.document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import jp.tonyu.db.DBAction;
import jp.tonyu.db.PrimaryKeySequence;
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
import net.arnx.jsonic.JSONException;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public class SDB extends SqlJetHelper implements DocumentSet {
	//public static final String PRIMARY_DBID_TXT = "primaryDbid.txt";

	static final int version=3;
	//public static final String UID_IMPORT = "77a729a1-5c5d-4d09-9141-72108ee9b634";
	//public static final String UID_EXISTENT_FILE = "86e08ee0-0bd5-4d1f-a7f5-66c2251e60ad";

	String dbid;
	final File dbFile;
	SFile blobDir;
	SFile backupDir;
	SFile homeDir;
	public static Map<File,SDB> insts=new HashMap<File, SDB>();
	public static int instc=0;
	public SDB(File file /*, String uid*/) throws SqlJetException {
		open(file, version);
		dbFile=file;
		homeDir=new SFile(file).parent();
		blobDir=homeDir.rel("blob");
		backupDir=homeDir.rel("backup");
		//this.dbid=uid;
		logManager=new LogManager(this);
		if (useIndex()) {
			idxSeq=new PrimaryKeySequence(this, indexTable());
		} else {
			idxSeq=null;
		}
		//setupDBID();
		//Log.d(this, "DBID = "+getDBID());
		dbid/*FromFile*/=getDBIDFromFile(new SFile(file.getAbsoluteFile()));
		//Log.d(this, compareDBID());
		instc++;
		//if (instc>=2) Log.die("Dup!!!");
		insts.put(file, this);
	}
	/*String dbidFromFile;
	public String compareDBID() {
		return "COMPAREDBID "+dbid+" - "+dbidFromFile;
	}*/
	Pattern dbidPat=Pattern.compile("\\w*\\.\\w+\\.");
	private String getDBIDFromFile(SFile file) {
		try {
			SFile parent=file.parent();
			/*SFile primaryDBID=parent.rel(PRIMARY_DBID_TXT);
			if (primaryDBID.exists()) {
				return primaryDBID.text();
			}*/
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
		return q(documentRecord,logRecord,indexRecord);//, dbidRecord/*,indexRecord*/);
	}
	public String toString() {
		return "(SDB dbid="+dbid+" home="+homeDir+")";
	}
	Map<String,DocumentRecord> cache=new HashMap<String, DocumentRecord>();
	final private LogManager logManager;

	final private PrimaryKeySequence idxSeq;
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
	public void save(final DocumentRecord d, final PairSet<String,String> indexValues) {
		save(d,indexValues,true);
	}
	private void save(final DocumentRecord d, final PairSet<String,String> indexValues,boolean realtimeBackup) {
	    LogRecord log=logManager.write("save",d.id);
	    d.lastUpdate=log.id;
	    if (realtimeBackup) {
	    	try {
	    		realtimeBackup(d);
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
	    }
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
	public boolean createdAtThisDB(DocumentRecord d) {
		return dbid.equals(dbidPart(d));
	}
	public String dbidPart(DocumentRecord d) {
		if (d==null || d.id==null) return null;
		String [] s=d.id.split("@");
		if (s.length<=1) return null;
		return s[1];
	}
	public int serialPart(DocumentRecord d) {
		if (d==null || d.id==null) return 0;
		String [] s=d.id.split("@");
		if (s.length<=0) return 0;
		return Integer.parseInt(s[0]);
	}

	public void restoreFromRealtimeBackup(SFile src, Set<String> updated) throws SqlJetException, JSONException, FileNotFoundException, IOException {
		DocumentRecord d=new DocumentRecord();
		Map<String,Object> m=(Map)JSON.decode(src.inputStream());
		d.copyFrom(m);
		DocumentRecord dst=byId(d.id);
		if (dst==null) {
			dst=newDocument(d.id);
		} else {
			if (dst.lastUpdate==d.lastUpdate) return;
		}
		d.copyTo(dst);
		save(dst, new PairSet<String, String>(),false);
		updated.add(dst.id);
		if (createdAtThisDB(dst)) {
			logManager.liftUpLastNumber(serialPart(dst));
		}
	}
	private void realtimeBackup(final DocumentRecord d)
			throws FileNotFoundException, IOException, NoSuchFieldException,
			IllegalAccessException {
		OutputStream outputStream = realtimeBackupFile(d).outputStream();
		JSON json = new JSON();
		json.setPrettyPrint(true);
		json.format( d.toMap() , outputStream  );
		outputStream.close();
	}
	private SFile realtimeBackupFile(DocumentRecord d) {
		return realtimeBackupDir().rel(d.id);
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
		i.id=idxSeq.next();
		i.document=d.id;
		i.name=name;
		i.value=value;
		i.lastUpdate=-d.lastUpdate;
		i.insertTo(indexTable());
	}
	private void removeIndexValues(final DocumentRecord d) throws SqlJetException {
		// use in writetransaction
		ISqlJetCursor cur= indexTable().scope("document", d.id, d.id);
		while (!cur.eof()) {
			indexRecord.fetch(cur);
			Log.d("updateIndex", "Remove index of "+d.id+" id="+indexRecord.id);
			cur.delete();
			//cur.next();
		}
		cur.close();
	}
	/*public void byIndex(final String name,final String value,final DocumentAction action) throws SqlJetException {
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
	}*/
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
			final PairSet<String,String> indexValues) {
		reserveWriteTransaction(new DBAction() {
			@Override
			public void run(SqlJetDb db) throws SqlJetException {
				updateIndexInTransaction(d, indexValues);
			}
		});
	}
	private void updateIndexInTransaction(final DocumentRecord d,
			final PairSet<String,String> indexValues) throws SqlJetException {
		if (useIndex()) {
			Log.d("updateIndex", "updateIndexIntrans "+d+" with "+indexValues);
			removeIndexValues(d);
			for (Pair<String, String> e:indexValues) {
				addIndexValue(d, e.key, e.value);
			}
		}
	}
	Boolean _useIndex=null;
	public boolean useIndex() {
		if (_useIndex!=null) return _useIndex;
		return _useIndex=indexTable().exists();
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
		Log.d("newestBackup", "Search backup "+backupDir+" *.json");
		for (SFile txt:backupDir) {
			if (!txt.name().endsWith(".json")) continue;
			if (src==null || txt.lastModified()>src.lastModified()) {
				src=txt;
			}
		}
		if (src==null) Log.die("Backup not found in "+backupDir);
		return src;
	}
	public SFile newBackupFile() {
		String d=new TDate().toString("yyyy_MMdd_hh_mm_ss");
		return backupDir.rel("main.db."+d+".json");
	}
	public Set<String> backupToJSON() throws SqlJetException, IOException {
		HashSet<String> backupedIDs=new HashSet<String>();
		Map<String, List<Map<String,Object>>> b=backup();
		List<Map<String,Object>> docs=b.get(documentRecord.tableName());
		for (Map<String,Object> doch:docs) {
			backupedIDs.add(doch.get("id")+"");
		}

		JSON json = new JSON();
		json.setPrettyPrint(true);
		OutputStream out = newBackupFile().outputStream();
		json.format(b, out);
		out.close();
		return backupedIDs;
	}
	public void restoreFromNewestJSON() throws IOException, SqlJetException {
		SFile src=newestBackupFile();
		InputStream in = src.inputStream();
		Map b=(Map)JSON.decode(in);
		in.close();
		restore(b);
	}
	public void cloneWithFilter(SDB dest, String[] ids) {
		for (String id:ids) {
			DocumentRecord d=byId(id);
			dest.save(d, new PairSet<String,String>());
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
	@Override
	public void searchByIndex(final Map<String,String> keyValues, final DocumentAction a) {
		if (keyValues.isEmpty()) {
			all(a);
			return;
		}
		try {
			readTransaction(new DBAction() {
				@Override
				public void run(SqlJetDb db) throws SqlJetException {
					final IntersectDocumentRecordIterator it=new IntersectDocumentRecordIterator();
					for (String key:keyValues.keySet()) {
						String value=keyValues.get(key);
						/*if ("owner".equals(value)) {
							it.add(new )
						}*/
						it.add(new IndexIterator(SDB.this, key, value));
					}
					while (it.hasNext()) {
						DocumentRecord d = it.next();
						if (a.run(d)) {
							break;
						}
					}
					it.close();
				}
			},-1);
		} catch (SqlJetException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void searchByIndex(final String key, final String value, final DocumentAction a) {
		try {
			Log.d("SearchByIndex", "["+key+"]=["+value+"]");
			readTransaction(new DBAction() {
				@Override
				public void run(SqlJetDb db) throws SqlJetException {
					DocumentRecordIterator it=new IndexIterator(SDB.this, key, value);
					while (it.hasNext()) {
						DocumentRecord d = it.next();
						if (a.run(d)) {
							break;
						}
					}
					it.close();

					/*SqlJetTableHelper t = table(indexRecord);
					String value2=value+(char)32767;
					ISqlJetCursor cur = t.scope(IndexRecord.NAME_VALUE_LAST_UPDATE, new Object[]{key,value,Long.MIN_VALUE},new Object[]{key,value2,Long.MIN_VALUE});
					while (!cur.eof()) {
						indexRecord.fetch(cur);
						DocumentRecord d = byId(indexRecord.document);
						if (a.run(d)) {
							break;
						}
						cur.next();
					}*/

				}
			}, -1);
		} catch (SqlJetException e) {
			e.printStackTrace();
		}
	}
	@Override
	public boolean indexAvailable(String key) {
		return "name".equals(key) || IndexRecord.INDEX_REFERS.equals(key);// || DocumentRecord.OWNER.equals(key);
	}
	public SFile realtimeBackupDir() {
		return homeDir.rel("rtBack");
	}
}
