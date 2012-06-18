package jp.tonyu.soytext2.document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.tonyu.db.DBAction;
import jp.tonyu.db.JDBCHelper;
import jp.tonyu.db.JDBCRecord;
import jp.tonyu.db.JDBCRecordCursor;
import jp.tonyu.db.JDBCTable;
import jp.tonyu.db.PrimaryKeySequence;
import jp.tonyu.debug.Log;
import jp.tonyu.util.SFile;
import jp.tonyu.util.TDate;
import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;


public class SDB  implements DocumentSet {
    //public static final String PRIMARY_DBID_TXT = "primaryDbid.txt";
    JDBCHelper helper;
    static final int version=3;

    @Override
    public void transaction(Object mode, final Runnable action) {
        try {
            if ("read".equalsIgnoreCase(mode+"")) {
                readTransaction(new DBAction() {
                    @Override
                    public void run(JDBCHelper db) throws SQLException {
                        action.run();
                    }
                }, -1);
            }
            else if ("write".equalsIgnoreCase(mode+"")) {
                writeTransaction(new DBAction() {
                    @Override
                    public void run(JDBCHelper db) throws SQLException {
                        action.run();
                    }
                }, -1);
            } else {
                Log.die("Invalid transaction mode: "+mode);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    //public static final String UID_IMPORT = "77a729a1-5c5d-4d09-9141-72108ee9b634";
    //public static final String UID_EXISTENT_FILE = "86e08ee0-0bd5-4d1f-a7f5-66c2251e60ad";

    public JDBCHelper getHelper() {
        return helper;
    }
    String dbid;
    final File dbFile;
    SFile blobDir;
    SFile backupDir;
    SFile homeDir;
    public static Map<File,SDB> insts=new HashMap<File, SDB>();
    public static int instc=0;

    public SDB(File file /*, String uid*/) throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        Connection conn =
                DriverManager.getConnection("jdbc:sqlite:"+file);
        helper=new JDBCHelper(conn,version) {

            @Override
            public Class<? extends JDBCRecord>[] tables(int version) {
                return q(DocumentRecord.class, IndexRecord.class, LogRecord.class);
            }
        };
        //open(file, version);
        dbFile=file;
        homeDir=new SFile(file).parent();
        blobDir=homeDir.rel("blob");
        backupDir=homeDir.rel("backup");
        logManager=new LogManager(this);
        idxSeq=new PrimaryKeySequence(indexTable());
        dbid=getDBIDFromFile(new SFile(file.getAbsoluteFile()));
        instc++;
        insts.put(file, this);
    }
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

    //DocumentRecord documentRecord=new DocumentRecord();
    //LogRecord logRecord=new LogRecord();
    //DBIDRecord dbidRecord=new DBIDRecord();
    //IndexRecord indexRecord=new IndexRecord();


    /*@Override
	public SqlJetRecord[] tables(int version) {
		return q(documentRecord,logRecord,indexRecord);//
	}*/

    public String toString() {
        return "(SDB dbid="+dbid+" home="+homeDir+")";
    }
    //Map<String,DocumentRecord> cache=new HashMap<String, DocumentRecord>();
    final private LogManager logManager;

    final private PrimaryKeySequence idxSeq;
    @Override
    public void all(final DocumentAction action) {
        all(action,true);
    }
    public void allWithoutTransaction(final DocumentAction action,final boolean fromNewest) throws SQLException {
        JDBCRecordCursor<DocumentRecord> cur;
        if (fromNewest) cur=docTable().order(DocumentRecord.LASTUPDATE_DESC);
        else cur = docTable().order(DocumentRecord.LASTUPDATE);
        while (cur.next()) {
            DocumentRecord d=cur.fetch();
            if (action.run(d)) break;
        }
        cur.close();
    }
    public void all(final DocumentAction action,final boolean fromNewest) {
        try {
            allWithoutTransaction(action, fromNewest);
            /*readTransaction(new DBAction() {
				@Override
				public void run(JDBCHelper db) throws SQLException {
				}
			},-1);*/
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public DocumentRecord byId(final String id) {
        try {
            DocumentRecord d;
            d=docTable().find1("id", id);
            return d;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }/*
	    synchronized (cache) {
			if (!cache.containsKey(id)) {
				try {
					readTransaction(new DBAction() {
						@Override
						public void run(JDBCHelper db) throws SQLException {
							DocumentRecord d=find1(documentRecord, null, id);
							if (d!=null) {
								cache.put(id, d);
							}

						}
					},-1);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			DocumentRecord res = cache.get(id);
			//if (res.content==null) Log.die(res.id+" has null content");
			return res;
		}*/
    }
    /*public void save(DocumentRecord d) {
		save(d,new HashMap<String,String>());
	}*/
    @Override
    public void save(final DocumentRecord d, final PairSet<String,String> indexValues) {
        try {
            save(d,indexValues,true);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    private void save(final DocumentRecord d, final PairSet<String,String> indexValues,boolean realtimeBackup) throws SQLException {
        LogRecord log=logManager.write("save",d.id);
        d.lastUpdate=log.id;
        if (realtimeBackup) {
            try {
                realtimeBackup(d);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /*reserveWriteTransaction(new DBAction() {
	        @Override
	        public void run(JDBCHelper db) throws SQLException {*/
        JDBCTable<DocumentRecord> t=docTable();
        DocumentRecord dd=t.find1("id", d.id);
        Log.d("SAVE", d);
        if (dd!=null) {
            t.update(d);
        } else {
            t.insert(d);
        }
        Log.d("SAVE", d+": done");
        updateIndexInTransaction(d, indexValues);
        /*}
	    });*/
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

    public void restoreFromRealtimeBackup(SFile src, Set<String> updated) throws SQLException, JSONException, FileNotFoundException, IOException {
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
    public int docCount() throws SQLException {
        JDBCTable<DocumentRecord> t=docTable();
        //JDBCRecordCursor<DocumentRecord> cur2=t.order();
        return t.rowCount();
    }
    /*public SqlJetTableHelper uidTable() throws SQLException {
		return table(dbidRecord);
	}*/
    public JDBCTable<DocumentRecord> docTable() throws SQLException {
        return table(DocumentRecord.class);
    }
    public JDBCTable<LogRecord> logTable() throws SQLException {
        return table(LogRecord.class);
    }
    public JDBCTable<IndexRecord> indexTable() throws SQLException {
        return table(IndexRecord.class);
    }
    @Override
    public DocumentRecord newDocument() {
        LogRecord log;
        try {
            log=logManager.write("create","<sameAsThisId>");
            DocumentRecord d=new DocumentRecord();
            d.id=log.id+"@"+dbid;
            d.lastUpdate=log.id;
            d.lastAccessed=log.id;
            //cache.put(d.id, d);
            return d;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    @Override
    public DocumentRecord newDocument(String id) {
        if (byId(id)!=null) Log.die(id +" already exists.");
        LogRecord log;
        try {
            log=logManager.write("create",id);
            DocumentRecord d=new DocumentRecord();
            d.id=id;
            d.lastUpdate=log.id;
            d.lastAccessed=log.id;
            //cache.put(d.id, d);
            return d;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    public void printLog () {
        logManager.printAll();
    }
    public void importLog(LogRecord curlog) throws SQLException {
        logManager.importLog(curlog);
    }
    public void all(LogAction action) {
        logManager.all(action);
    }
    @Override
    public int log(String date, String action, String target, String option) {
        LogRecord l;
        try {
            l=logManager.write(action, target);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return l.id;
    }
    private void addIndexValue(DocumentRecord d,String name, String value) throws SQLException {
        // use in writetransaction
        IndexRecord i = new IndexRecord();
        i.id=idxSeq.next();
        i.document=d.id;
        i.name=name;
        i.value=value;
        i.lastUpdate=d.lastUpdate;
        indexTable().insert(i);
    }
    private void removeIndexValues(final DocumentRecord d) throws SQLException {
        // use in writetransaction
        indexTable().delete("document", d.id, d.id);
        /*JDBCRecordCursor<IndexRecord> cur=indexTable().scope("document", d.id, d.id);

		while (cur.next()) {
			SqlJetRecord.fetch(indexRecord , cur);
			Log.d("updateIndex", "Remove index of "+d.id+" id="+indexRecord.id);
			cur.delete();
			//cur.next();
		}
		cur.close();*/
    }
    /*public void byIndex(final String name,final String value,final DocumentAction action) throws SQLException {
		readTransaction(new DBAction() {

			@Override
			public void run(JDBCHelper db) throws SQLException {
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
    /*public void resetIndex() throws SQLException {
		writeTransaction(new DBAction() {

			@Override
			public void run(JDBCHelper db) throws SQLException {
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
    /*private DocumentRecord fetchDocumentFromCursorOrCache(
			SqlJetRecordCursor<DocumentRecord> cur, String id)
			throws SQLException {
		DocumentRecord d=null;
		synchronized (cache) {
			d=cache.get(id);
			if (d==null) {
				d=cur.fetch();
				cache.put(id,d);
			}
		}
		return d;
	}*/
    @Override
    public void updateIndex(final DocumentRecord d,
            final PairSet<String,String> indexValues) {
        try {
            /*reserveWriteTransaction(new DBAction() {
            	@Override
            	public void run(JDBCHelper db) throws SQLException {*/
            updateIndexInTransaction(d, indexValues);
            /*	}
	        });*/
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    private void updateIndexInTransaction(final DocumentRecord d,
            final PairSet<String,String> indexValues) throws SQLException {
        Log.d("updateIndex", "updateIndexIntrans "+d+" with "+indexValues);
        removeIndexValues(d);
        for (Pair<String, String> e:indexValues) {
            addIndexValue(d, e.key, e.value);
        }
    }
    /*Boolean _useIndex=null;
	public boolean useIndex() {
		if (_useIndex!=null) return _useIndex;
		return _useIndex=indexTable().exists();
	}*/
    public static final String MIN_STRING="",MAX_STRING=new String(new char[]{65535,65535,65535});
    /*
	public void addIndexName(final String name) throws SQLException {
		Set<String> s = indexNames();
		if (s.contains(name)) return;
		reserveWriteTransaction(new DBAction() {

			@Override
			public void run(JDBCHelper db) throws SQLException {
				IndexRecord i = new IndexRecord();
				i.name= IndexRecord.DEFINED_INDEX_NAMES;
				i.value=name;
				i.insertTo(indexTable());
				indexNames.add(name);
			}
		});
	}
	HashSet<String> indexNames = new HashSet<String>();

	public Set<String> indexNames() throws SQLException {
		if (indexNames!=null) return indexNames;
		indexNames = new HashSet<String>();
		readTransaction(new DBAction() {

			@Override
			public void run(JDBCHelper db) throws SQLException {
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
    public Set<String> backupToJSON() throws SQLException, IOException {
        HashSet<String> backupedIDs=new HashSet<String>();
        Map<String, List<Map<String,Object>>> b=backup();
        List<Map<String,Object>> docs=b.get(new DocumentRecord().tableName());
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
    public void restoreFromNewestJSON() throws IOException, SQLException {
        SFile src=newestBackupFile();
        InputStream in = src.inputStream();
        Map b=(Map)JSON.decode(in);
        in.close();
        restore(b);
    }
    public void cloneWithFilter(final SDB dest, String[] ids) throws SQLException {
        final Set<String> idss=new HashSet<String>();
        for (String id:ids) {
            DocumentRecord d=byId(id);
            idss.add(id);
            dest.save(d, new PairSet<String,String>());
        }
        /*readTransaction(new DBAction() {

			@Override
			public void run(JDBCHelper db) throws SQLException {*/
        JDBCTable<IndexRecord> t=table(IndexRecord.class);
        JDBCRecordCursor<IndexRecord> cur=t.order();
        while (cur.next()) {
            final IndexRecord indexRecord=cur.fetch();
            if (idss.contains(indexRecord.id)) {
                dest.reserveWriteTransaction(new DBAction() {
                    @Override
                    public void run(JDBCHelper db) throws SQLException {
                        dest.addIndexValue(byId(indexRecord.document), indexRecord.name, indexRecord.value);
                    }
                });
            }
        }
        /*	}
		}, -1);*/
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
    public void searchByIndex(final Map<String,String> keyValues, final IndexAction a) {
        /*if (keyValues.isEmpty()) {
			all(a);
			return;
		}*/
        try {
            /*readTransaction(new DBAction() {
				@Override
				public void run(JDBCHelper db) throws SQLException {*/
            final IntersectIndexIterator it=new IntersectIndexIterator();
            for (String key:keyValues.keySet()) {
                String value=keyValues.get(key);
                it.add(new SingleIndexIterator(SDB.this, key, value));
            }
            while (it.hasNext()) {
                IndexRecord d = it.next();
                if (a.run(d)) {
                    break;
                }
            }
            it.close();
            /*	}
			},-1);*/
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void searchByIndex(final String key, final String value, final IndexAction a) {
        try {
            Log.d("SearchByIndex", "["+key+"]=["+value+"]");
            /*readTransaction(new DBAction() {
				@Override
				public void run(JDBCHelper db) throws SQLException {*/
            IndexIterator it=new SingleIndexIterator(SDB.this, key, value);
            while (it.hasNext()) {
                IndexRecord d = it.next();
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

            /*	}
			}, -1);*/
        } catch (SQLException e) {
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
    //---delegates
    public <T extends JDBCRecord> JDBCTable<T> table(Class<T> class1) throws SQLException {
        return helper.table(class1);
    }
    public void readTransaction(DBAction dbAction, int i) throws SQLException {
        helper.readTransaction(dbAction, i);
    }
    public void close() throws SQLException {
        helper.close();
    }
    public Map<String, List<Map<String, Object>>> backup() throws SQLException {
        return helper.backup();
    }
    public int execUpdate(String q) throws SQLException {
        return helper.execUpdate(q);
    }
    public Statement createStatement() throws SQLException {
        return helper.createStatement();
    }
    public boolean equals(Object arg0) {
        return helper.equals(arg0);
    }
    public void exec(String q) throws SQLException {
        helper.exec(q);
    }
    public ResultSet execQuery(String q) throws SQLException {
        return helper.execQuery(q);
    }
    public ResultSet execQuery(String q, Object... args) throws SQLException {
        return helper.execQuery(q, args);
    }
    public int execUpdate(String q, Object... args) throws SQLException {
        return helper.execUpdate(q, args);
    }
    public int hashCode() {
        return helper.hashCode();
    }
    public void writeTransaction(DBAction action, int timeOut)
            throws SQLException {
        helper.writeTransaction(action, timeOut);
    }
    public void reserveWriteTransaction(DBAction action) throws SQLException {
        helper.reserveWriteTransaction(action);
    }
    public <T extends JDBCRecord> JDBCTable<T> table(String name) {
        return helper.table(name);
    }
    public <T extends JDBCRecord> JDBCTable<T> table(T r) {
        return helper.table(r);
    }
    public Class<? extends JDBCRecord>[] tables(int version) {
        return helper.tables(version);
    }
    public void update(JDBCRecord r) throws SQLException,
    IllegalArgumentException, IllegalAccessException,
    NoSuchFieldException {
        helper.update(r);
    }
    public Map<String, JDBCRecord> tablesAsMap(int version) {
        return helper.tablesAsMap(version);
    }
    public int version() {
        return helper.version();
    }
    public void restore(Map<String, List<Map<String, Object>>> data)
            throws SQLException {
        helper.restore(data);
    }

}
