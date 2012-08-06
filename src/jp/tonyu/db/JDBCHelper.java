package jp.tonyu.db;

import java.io.File;
import java.io.Writer;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jp.tonyu.debug.Log;
import jp.tonyu.util.MapAction;
import jp.tonyu.util.Maps;
import jp.tonyu.util.Util;

/**
 * The helper class which provides some useful routine (transaction, versioning etc.)
 * and simple O-R mapper.
 * @author hoge1e3
 *
 */
public abstract class JDBCHelper {
    static final String VERSION_TABLE = "jdbc_helper_version";
    VersionRecord vr=new VersionRecord();
    protected Connection db;
    private int version;
    public JDBCHelper(Connection db, final int version) throws SQLException {
        this.db=db;
        db.setAutoCommit(true);
        if (version<=0) {
            throw new RuntimeException("Version must be >0");
        }
        initTable(VersionRecord.class);
        writeTransaction(new WriteAction() {
            @Override
            public void run(JDBCHelper jdbcHelper)
                    throws NotInWriteTransactionException, SQLException {
                int oldVer=getOldVersion();
                initTables();
                if (oldVer==0) {
                    create(version);
                } else if (oldVer!=version) {
                    upgrade(oldVer,version);
                }
                JDBCHelper.this.version=version;
            }
        });
    }

    private void initTables() throws SQLException {
        for (Class<? extends JDBCRecord> t:tables(version)) {
            //JDBCRecord r=recInstance(t);
            initTable(t);
        }
    }
    private <T extends JDBCRecord> void createTableAndIndex(Class<T> r) throws SQLException {
        JDBCTable<T> tbl=table(r);
        tbl.create();
        tbl.createAllIndex();
    }
    private <T extends JDBCRecord>void initTable(Class<T> t) throws SQLException {
        JDBCTable<T> jdbcTable = new JDBCTable<T>(this , t);
        tables.put(recInstance(t).tableName(), jdbcTable);
    }

    private int getOldVersion() throws SQLException, NotInReadTransactionException {
        int res=0;
        if (!exists(VersionRecord.class)) return res;
        JDBCRecordCursor<VersionRecord> r = table(VersionRecord.class).all();
        while (r.next()) {
            VersionRecord v = r.fetch();
            res=v.version;
        }
        r.close();
        return res;
    }

    private boolean exists(Class<VersionRecord> class1) throws SQLException {
        return table(class1).exists();
    }

    int readCount=0;
    //SqlJetTransactionMode transaction=null;
    public void writeTransaction(WriteAction action) throws SQLException {
        try {
            Log.d("JDBC","Trans Start");
            db.setAutoCommit(false);
            try {
                action.run(this);
            } catch (NotInWriteTransactionException e) {
                e.printStackTrace();
            }
            db.commit();
            db.setAutoCommit(true);
            Log.d("JDBC","Trans End");
            action.afterCommit(this);
        } catch(SQLException e) {
            e.printStackTrace();
            db.rollback();
            action.afterRollback(this);
        }
    }
    /*public void reserveWriteTransaction(WriteAction action) throws SQLException {
        writeTransaction(action);
    }*/


    public void readTransaction(ReadAction action) throws SQLException {
        try {
            Log.d("JDBC", "Read trans start");
            db.setAutoCommit(false);
            try {
                action.run(this);
            } catch (NotInReadTransactionException e) {
                e.printStackTrace();
            }
            db.commit();
            Log.d("JDBC", "Read trans end");
        } finally {
            //db.setAutoCommit(true);
            action.afterCommit(this);
        }
    }

    private void create(int newVersion) throws SQLException, NotInWriteTransactionException {
        onCreate(db,newVersion);
        setVersion(newVersion);
    }
    private void setVersion(int newVersion) throws SQLException, NotInWriteTransactionException {
        JDBCTable<VersionRecord> table = table(vr);
        if (!table.exists()) {
            createTableAndIndex(VersionRecord.class);
        }
        table.deleteAll();
        vr.version=newVersion;
        table.insert(vr);
    }

    private void upgrade(int oldVersion, int newVersion) throws SQLException, NotInWriteTransactionException {
        onUpgrade(db,oldVersion,newVersion);
        setVersion(newVersion);
    }
    protected void onUpgrade(Connection db, int oldVersion, int newVersion) throws SQLException {
        //System.out.println("Version "+oldVersion+" -> "+newVersion);
    }
    private <T extends JDBCRecord> T recInstance(Class<T> klass) throws SQLException {
        try {
            T r=klass.newInstance();
            return r;
        } catch (InstantiationException e) {
            throw new SQLException(e);
        } catch (IllegalAccessException e) {
            throw new SQLException(e);
        }

    }
    protected void onCreate(final Connection db, int version) throws SQLException {
        //System.out.println("Created");
        for (Class<? extends JDBCRecord> t: tables(version)) {
            //JDBCRecord r=recInstance(t);
            createTableAndIndex(t);

        }
    }
    /*public static void main(String[] args) throws SQLException {
		new SqlJetHelper(new File("empty.db"), 3);
	}*/
    boolean closing=false;
    public void close() throws SQLException {
        Log.d(this,"Closing..");
        closing=true;

        /*try {
			reservedTransactionThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
        db.close();
        Log.d(this,"Closed");
    }
    Map<String, JDBCTable> tables=new HashMap<String, JDBCTable>();
    public <T extends JDBCRecord> JDBCTable<T> table(String name) {
        return tables.get(name);//new JDBCTable(this , name);
    }
    public <T extends JDBCRecord> JDBCTable<T> table(T r) {
        return table(r.tableName());
    }
    public <T extends JDBCRecord> JDBCTable<T> table(Class<T> r) throws SQLException {
        return table(recInstance(r).tableName());
    }
    public abstract Class<? extends JDBCRecord>[] tables(int version);/* {
		return q();
	}*/
    public static <T> T[] q(T...ts) {
        return ts;
    }
    public void update(JDBCRecord r) throws SQLException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, NotInWriteTransactionException {
        table(r).update(r);
    }

    /*public <T extends JDBCRecord> T find1(T record, String columnSpec, Object... values) throws SQLException {
        JDBCTable<T> t = table(record);
        ResultSet cur = t.lookup(columnSpec, values);
        T res=null;
        if (cur.next()) {
            res = record.dup(record);
            fetch(res, cur);
        }
        cur.close();
        return res;
    }*/
    public static void fetch(JDBCRecord rec, ResultSet cur) throws SQLException {
        int i=0;
        for (String fname:rec.columnOrder()) {
            try {
                Field f = rec.getField(fname);
                Class<?> t=f.getType();
                if (t.equals(Long.TYPE)) {
                    long r=cur.getInt(i+1);
                    f.set(rec, r);
                } else if (t.equals(Integer.TYPE)) {
                    int r=(int)cur.getInt(i+1);
                    f.set(rec, r);
                } else if (String.class.isAssignableFrom(t)) {
                    String r=cur.getString(i+1);
                    f.set(rec, r);
                } else {
                    Log.die(t+" cannot be assigned");
                }
                i++;
            } catch (NoSuchFieldException e) {
                throw new SQLException(e);
            } catch (IllegalArgumentException e) {
                throw new SQLException(e);
            } catch (IllegalAccessException e) {
                throw new SQLException(e);
            }

        }
    }
    /*public <T extends JDBCRecord> JDBCRecordCursor<T> order(T record, String... columnNames) throws SQLException {
        JDBCTable<T> t = table(record);
        ResultSet cur = t.order(columnNames);
        return new JDBCRecordCursor<T>(record, cur);
    }*/

    /*public <T extends JDBCRecord> JDBCRecordCursor<T> order(Class<T> record, String... columnNames) throws SQLException {
        return order(recInstance(record),columnNames);
        //ResultSet cur = t.order(columnNames);
        //return new JDBCRecordCursor<T>((T) recInstance(record), cur);
    }*/
    /*public <T extends JDBCRecord> JDBCRecordCursor<T> reverseOrder(T record, String attrNames) throws SQLException {
		JDBCTable t = table(record);
		ResultSet cur = t.order(attrNames);
		return new JDBCRecordCursor<T>(record, cur.reverse());
	}*/
    public Map<String, List<Map<String,Object>>> backup() throws SQLException {
        final Map<String, List<Map<String,Object>>> res=new HashMap<String, List<Map<String,Object>>>();
        readTransaction(new ReadAction() {

            @Override
            public void run(JDBCHelper db) throws SQLException, NotInReadTransactionException {
                for (Class<? extends JDBCRecord> tclass: tables(version)) {
                    JDBCTable<? extends JDBCRecord> t=table(tclass);
                    if (!t.exists()) continue;
                    JDBCRecordCursor<? extends JDBCRecord> cur = t.all();
                    List<Map<String,Object>> list=new Vector<Map<String,Object>>();
                    res.put(t.name(), list);
                    while (cur.next()) {
                        JDBCRecord re = cur.fetch();
                        try {
                            Log.d("Export", re);
                            list.add(re.toMap());
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new SQLException(e);
                        }
                        cur.next();
                    }
                    cur.close();
                }
            }
        });
        return res;
    }
    public Map<String, JDBCRecord> tablesAsMap(int version) {
        Map<String, JDBCRecord> res=new HashMap<String, JDBCRecord>();
        for (Class<? extends JDBCRecord> t: tables(version)) {
            JDBCRecord r;
            try {
                r = t.newInstance();
            } catch (InstantiationException e1) {
                throw new RuntimeException(e1);
            } catch (IllegalAccessException e1) {
                throw new RuntimeException(e1);
            }
            res.put(r.tableName(), r);
        }
        return res;
    }
    public int execUpdate(String q) throws SQLException {
        Statement st = createStatement();
        int r=st.executeUpdate(q);
        st.close();
        return r;
    }
    public Statement createStatement() throws SQLException {
        return db.createStatement();
    }
    public void exec(String q) throws SQLException {
        debugQuery(q);
        Statement st = createStatement();
        st.execute(q);
        st.close();
    }

    private void debugQuery(String q) {
        Log.d("query", q);
    }
    public ResultSet execQuery(String q) throws SQLException, NotInReadTransactionException {
        debugQuery(q);
        Statement st = createStatement();
        ResultSet r = st.executeQuery(q);
        return new JDBCCursor(st,r);
    }
    public ResultSet execQuery(String q,Object... args) throws SQLException, NotInReadTransactionException {
        debugQuery(q);
        PreparedStatement st = prepareStatement(q, args);
        ResultSet r = st.executeQuery();
        return new JDBCCursor(st,r);
    }
    private PreparedStatement prepareStatement(String q, Object... args)
            throws SQLException {
        PreparedStatement st = db.prepareStatement(q);
        Log.d("query-prep", q+" args="+Util.join(",", args));
        for (int i=0 ; i<args.length; i++) {
            if (args[i] instanceof String) {
                String str = (String) args[i];
                st.setString(i+1, str);
            }
            if (args[i] instanceof Long) {
                Long l = (Long) args[i];
                st.setLong(i+1, l);
            }
            if (args[i] instanceof Integer) {
                Integer l = (Integer) args[i];
                //Log.d("q", "i="+i+"  l="+l);
                st.setLong(i+1, l);
            }
        }
        //Log.d("query-prep", st);
        return st;
    }
    public int version() {return version;}
    public int execUpdate(String q,Object... args) throws SQLException, NotInWriteTransactionException {
        debugQuery(q);
        PreparedStatement st = prepareStatement(q, args);
        return st.executeUpdate();
    }

    //                            table  record    field  value
    public void restore(final Map<String, List<Map<String,Object>>> data) throws SQLException {
        final Map<String, JDBCRecord> tables=tablesAsMap(version);
        writeTransaction(new WriteAction() {

            @Override
            public void run(JDBCHelper db) throws SQLException, NotInWriteTransactionException {
                for (Map.Entry<String, List<Map<String,Object>>> e:data.entrySet()) {
                    String key=e.getKey();
                    List<Map<String,Object>> value=e.getValue();
                    JDBCRecord r = tables.get(key);
                    if (r==null) {
                        Log.d("db:restore", "Table "+key+" Not found ");
                        return;
                    }
                    JDBCTable<?> t = table(r.tableName());
                    t.deleteAll();
                    for (Map<String,Object> m:value) {
                        r.copyFrom(m);
                        t.insert(r);
                    }
                }
            }
        });
    }

    public void commit() throws SQLException {
        db.commit();
    }
}
