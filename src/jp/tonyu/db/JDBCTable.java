package jp.tonyu.db;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;
import static jp.tonyu.db.JDBCRecord.q;
import jp.tonyu.debug.Log;
import jp.tonyu.util.Util;

public class JDBCTable<T extends JDBCRecord> {
    JDBCHelper db;
    // String name;
    T rec;
    public JDBCTable(JDBCHelper db, Class<T> recordClass) throws SQLException {
        this.db=db;
        rec=JDBCRecord.newInstance(recordClass);
        // this.name=name;
        initColumns();
    }
    public String name() {
        return rec.tableName();
    }
    public boolean exists() {
        try {
            db.exec("select * from "+nameSym()+" limit 1;");
        } catch (SQLException e) {
            return false;
        }
        return true;
    }
    List<Column> columns=new Vector<Column>();
    private JDBCTable<T> addColumn(String name, String type) {
        columns.add(new Column(name, type));
        return this;
    }
    public JDBCTable<T> create() throws SQLException {
        StringBuilder buf=new StringBuilder("CREATE TABLE "+nameSym()+"(\n");
        String com="";
        for (Column a : columns) {
            buf.append(com+a);
            com=",\n";
        }
        buf.append(")");
        debugQuery(buf.toString());
        db.execUpdate(buf.toString());
        return this;
    }
    /*
     * public JDBCTable<T> createIndex(OrderBy o) throws SQLException { return
     * createIndex(o); }
     */
    /*
     * private String indexName(String[] columnNames) { return
     * name()+"_"+Util.join("__",columnNames).replaceAll("\\s",
     * "").replaceAll("\\W", "_"); }
     */
    public JDBCTable<T> createIndex(OrderBy o) throws SQLException {
        String indexName=o.indexName(name());// indexName(o);
        String q="CREATE INDEX "+indexName+" ON "+nameSym()+" ("+o+")";
        debugQuery(q);
        db.execUpdate(q);
        return this;
    }
    public void debugQuery(String q) {
        Log.d(this, "Query - "+q);
    }
    private String selectFrom() {
        return "select * from "+nameSym();
    }
    private String deleteFrom() {
        return "delete from "+nameSym();
    }
    public JDBCRecordCursor<T> order() throws SQLException, NotInReadTransactionException {
        ResultSet cur=db.execQuery(selectFrom()+";");
        return new JDBCRecordCursor<T>(rec, cur);
    }
    public JDBCRecordCursor<T> order(String orderSpec) throws SQLException, NotInReadTransactionException {
        return order(OrderBy.parse(orderSpec));
    }
    public JDBCRecordCursor<T> order(OrderBy ord) throws SQLException, NotInReadTransactionException {
        ResultSet c=db.execQuery(selectFrom()+" order by "+ord+";");
        return new JDBCRecordCursor<T>(rec, c);
    }
    static String columnNameList(String... columnNames) {
        StringBuilder buf=new StringBuilder();
        String cmd="";
        for (String a : columnNames) {
            buf.append(cmd+symbol(a));
            cmd=",";
        }
        return buf+"";
    }
    public static String columnNameList(Column... columns) {
        StringBuilder buf=new StringBuilder();
        String cmd="";
        for (Column a : columns) {
            buf.append(cmd+symbol(a.name));
            cmd=",";
        }
        return buf+"";
    }
    static String symbol(String a) {
        return "\""+a+"\"";
    }
    public void createAllIndex() throws SQLException {
        for (String iname : rec.indexSpecs()) {
            OrderBy o=OrderBy.parse(iname);
            createIndex(o);
        }
    }
    public void initColumns() throws SQLException {
        columns.clear();
        for (String fname : rec.columnOrder()) {
            Field f;
            try {
                f=rec.getField(fname);
            } catch (NoSuchFieldException e) {
                throw new SQLException(e);
            }
            String type="TEXT";
            Class ftype=f.getType();
            if (ftype.equals(Long.TYPE)||ftype.equals(Integer.TYPE)) {
                type="INTEGER";
            }
            if (fname.equals(rec.primaryKeyName())) {
                type+=" PRIMARY KEY";
            }
            addColumn(fname, type);
        }
    }
    public JDBCRecordCursor<T> lookup(String columnSpec, Object... objects)
            throws SQLException, NotInReadTransactionException {
        return lookup(OrderBy.parse(columnSpec), objects);
    }
    public JDBCRecordCursor<T> lookup(OrderBy ord, Object... objects)
            throws SQLException, NotInReadTransactionException {
        StringBuilder buf=new StringBuilder();
        String cmd="";
        for (OrderByElem o : ord) {
            buf.append(cmd+symbol(o.name)+"=?");
            cmd=" and ";
        }
        return new JDBCRecordCursor<T>(rec, db.execQuery(selectFrom()+" where "
                +buf+" order by "+ord+";", objects));
    }
    public JDBCRecordCursor<T> scope(String columnSpec, Object[] from,
            Object[] to) throws SQLException, NotInReadTransactionException {
        return scope(OrderBy.parse(columnSpec), from, to);
    }
    public JDBCRecordCursor<T> scope(OrderBy ord, Object[] from, Object[] to)
            throws SQLException, NotInReadTransactionException {
        Where w=where(ord, from, to);
        return new JDBCRecordCursor<T>(rec, db.execQuery(selectFrom()+" where "
                +w.buf+" order by "+ord+";", w.values));
    }
    public int delete(OrderBy ord, Object[] from, Object[] to)
            throws SQLException, NotInWriteTransactionException {
        Where w=where(ord, from, to);
        return db.execUpdate(deleteFrom()+" where "+w.buf+";", w.values);
    }
    private Where where(OrderBy ord, Object[] from, Object[] to) {
        StringBuilder buf=new StringBuilder();
        String cmd="";
        Object[] values=new Object[ord.size()*2];
        for (int i=0; i<ord.size(); i++) {
            Object f=from[i];
            Object t=to[i];
            values[i*2]=f;
            values[i*2+1]=t;
            String cname=symbol(ord.get(i).name);
            buf.append(cmd+cname+">=? and "+cname+"<=?");
            cmd=" and ";
        }
        Where w=new Where(buf+"", values);
        return w;
    }
    public JDBCRecordCursor<T> scope(String singleColumnSpec, Object from,
            Object to) throws SQLException, NotInReadTransactionException {
        return scope(OrderBy.parse(singleColumnSpec),q(from),q(to));
    }
    public int insertValues(Object... values) throws SQLException, NotInWriteTransactionException {
        return db.execUpdate("insert into "+nameSym()+"("
                +columnNameList(columns.toArray(new Column[0]))+") values ("
                +questions(values.length)+");", values);
    }
    public int insert(JDBCRecord r) throws SQLException, NotInWriteTransactionException {
        return insertValues(r.toValues());
    }
    public int updateValues(Object primaryKeyValue, Object... values)
            throws SQLException, NotInWriteTransactionException {
        Object[] values_prim=new Object[values.length+1];
        for (int i=0; i<values.length; i++) {
            values_prim[i]=values[i];
        }
        values_prim[values_prim.length-1]=primaryKeyValue;
        String str="update "+nameSym()+" set "+set()+" where "
                +symbol(primaryKeyName())+"=?";
        Log.d(this, "update str="+str);
        return db.execUpdate(str, values_prim);
    }
    public String primaryKeyName() {
        return rec.primaryKeyName();
    }
    public void update(JDBCRecord r) throws SQLException, NotInWriteTransactionException {
        try {
            Object[] values=r.toValues(true);
            Log.d(this, "update - "+Util.join(", ", values));
            updateValues(r.getField(r.primaryKeyName()).get(r),values);
        } catch (IllegalArgumentException e) {
            throw new SQLException(e);
        } catch (IllegalAccessException e) {
            throw new SQLException(e);
        } catch (NoSuchFieldException e) {
            throw new SQLException(e);
        }
    }
    public T find1(String columnSpec, Object... values) throws SQLException, NotInReadTransactionException {
        JDBCRecordCursor<T> c=lookup(columnSpec, values);
        T res=null;
        while (c.next()) {
            res=c.fetch();
            break;
        }
        c.close();
        return res;
    }
    private String set() {
        String primaryKeyName=primaryKeyName();
        StringBuilder b=new StringBuilder();
        String com="";
        for (Column c : columns) {
            if (c.name.equals(primaryKeyName)) continue;
            b.append(com+symbol(c.name)+"=?");
            com=",";
        }
        return b+"";
    }
    private String questions(int length) {
        StringBuilder buf=new StringBuilder();
        for (int i=0; i<length; i++) {
            if (i>0) buf.append(",");
            buf.append("?");
        }
        return buf+"";
    }
    public int deleteAll() throws SQLException {
        return db.execUpdate("delete from "+nameSym());
    }
    private String nameSym() {
        return symbol(name());
    }
    public JDBCRecordCursor<T> all() throws SQLException, NotInReadTransactionException {
        return order();
    }
    public JDBCHelper getDB() {
        return db;
    }
    public int max(String columnName) throws SQLException, NotInReadTransactionException {
        ResultSet r=db.execQuery("select max("+symbol(columnName)+") from "
                +nameSym()+";");
        int res=0;
        while (r.next()) {
            res=r.getInt(1);
        }
        r.close();
        return res;
    }
    public int rowCount() throws SQLException, NotInReadTransactionException {
        ResultSet r=db.execQuery("select count("+symbol(primaryKeyName())
                +") from "+nameSym()+";");
        int res=0;
        while (r.next()) {
            res=r.getInt(1);
        }
        r.close();
        return res;
    }
    public void delete(String columnSpec, String from, String to) throws SQLException, NotInWriteTransactionException {
        delete(OrderBy.parse(columnSpec), q(from),q(to));
    }
}
