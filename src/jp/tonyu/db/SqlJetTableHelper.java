package jp.tonyu.db;

import java.util.List;
import java.util.Vector;

import jp.tonyu.debug.Log;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.internal.table.SqlJetTable;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public class SqlJetTableHelper {
	SqlJetDb db; 
	String name;
	public SqlJetTableHelper(SqlJetDb db ,String name) {
		this.db=db;
		this.name=name;
	}
	public ISqlJetTable table() throws SqlJetException {
		return db.getTable(name);
	}
	List<String> attrs=new Vector<String>();
	public SqlJetTableHelper a(String name ,String type) {
		attrs.add(name+" "+type);
		return this;
	}
	public SqlJetTableHelper create() throws SqlJetException {
		StringBuilder buf = new StringBuilder("CREATE TABLE "+name+"(\n");
		String com="";
		for (String a:attrs) {
			buf.append(com+a);
			com=",\n";
		}
		buf.append(")");
		debugQuery(buf.toString());
		db.createTable(buf.toString());
		return this;
	}
	public SqlJetTableHelper createIndex(String attrNames) throws SqlJetException {
		String indexName=indexName(attrNames);
		return createIndex(indexName, attrNames);
	}
	private String indexName(String attrNames) {
		return name+"_"+attrNames.replaceAll("\\s", "").replaceAll("\\W", "_");
	}		
	public SqlJetTableHelper createIndex(String indexName, String attrNames) throws SqlJetException {
		String q = "CREATE INDEX "+indexName+" ON "+name+" ("+attrNames+")";
		debugQuery(q);
		db.createIndex(q);
		return this;
	}
	public void debugQuery(String q) {
		Log.d(this, "Query - "+q);
	}
	public ISqlJetCursor order() throws SqlJetException {
		return table().order(null);
	}
	public ISqlJetCursor orderByIndexName(String indexName) throws SqlJetException {
		return table().order(indexName);
	}
	public ISqlJetCursor order(String attrNames) throws SqlJetException {
		return orderByIndexName(indexName(attrNames));
	}
	public ISqlJetCursor lookup(Object pkey) throws SqlJetException {
		return table().lookup(null, pkey);
	}
	public ISqlJetCursor lookup(String attrNames, Object... objects) throws SqlJetException {
		return table().lookup(indexName(attrNames), objects);
	}
	public void insert(Object... object) throws SqlJetException {
		table().insert(object);
	}
	
}
