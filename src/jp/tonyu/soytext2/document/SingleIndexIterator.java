package jp.tonyu.soytext2.document;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import jp.tonyu.db.JDBCRecordCursor;
import jp.tonyu.db.JDBCTable;

public class SingleIndexIterator implements IndexIterator {
	JDBCRecordCursor<IndexRecord>  cur;
	SDB sdb;
	String key,value;

	boolean hasNexted=false, lastHasNext;
	public SingleIndexIterator(SDB sdb, String key, String value) throws SQLException {
		this.sdb=sdb;
		JDBCTable<IndexRecord> t = sdb.table(IndexRecord.class);
		String value2=value+(char)32767;
		cur = t.scope(IndexRecord.NAME_VALUE_LAST_UPDATE, new Object[]{key,value,Long.MIN_VALUE},new Object[]{key,value2,Long.MAX_VALUE});
		//cur = t.scope("name,value", new Object[]{key,value},new Object[]{key,value});
        this.key=key;
		this.value=value;
	}
	@Override
	public boolean hasNext() throws SQLException{
	    if (hasNexted) return lastHasNext;
	    hasNexted=true;
		return lastHasNext=cur.next();
	}

	@Override
	public String toString() {
		return "(IndexIterator "+key+"="+value+")";
	}
	@Override
	public IndexRecord next() throws SQLException{
	    if (!hasNexted) {
	        if (!hasNext()) throw new NoSuchElementException();
	    }
        hasNexted=false;
		IndexRecord r=cur.fetch();
		//DocumentRecord d = sdb.byId(r.document);
		return r;
	}
	@Override
	public void close() throws SQLException {
		cur.close();
	}

}
