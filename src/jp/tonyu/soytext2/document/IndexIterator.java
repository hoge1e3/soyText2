package jp.tonyu.soytext2.document;

import java.sql.SQLException;

import jp.tonyu.db.JDBCRecordCursor;
import jp.tonyu.db.JDBCTable;

public class IndexIterator implements DocumentRecordIterator {
	JDBCRecordCursor<IndexRecord>  cur;
	SDB sdb;
	String key,value;
	public IndexIterator(SDB sdb, String key, String value) throws SQLException {
		this.sdb=sdb;
		JDBCTable<IndexRecord> t = sdb.table(IndexRecord.class);
		String value2=value+(char)32767;
		cur = t.scope(IndexRecord.NAME_VALUE_LAST_UPDATE, new Object[]{key,value,Long.MIN_VALUE},new Object[]{key,value2,Long.MIN_VALUE});
		this.key=key;
		this.value=value;
	}
	@Override
	public boolean hasNext() throws SQLException{
		return cur.next();
	}

	@Override
	public String toString() {
		return "(IndexIterator "+key+"="+value+")";
	}
	@Override
	public DocumentRecord next() throws SQLException{
		IndexRecord r=cur.fetch();
		//SqlJetRecord.fetch( sdb.indexRecord, cur);
		//cur.next();
		DocumentRecord d = sdb.byId(r.document);
		return d;
	}
	@Override
	public void close() throws SQLException {
		cur.close();
	}

}
