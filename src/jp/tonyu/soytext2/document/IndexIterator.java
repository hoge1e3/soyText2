package jp.tonyu.soytext2.document;

import java.util.Iterator;

import jp.tonyu.db.SqlJetRecord;
import jp.tonyu.db.SqlJetTableHelper;
import jp.tonyu.soytext2.js.DocumentScriptable;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

public class IndexIterator implements DocumentRecordIterator {
	ISqlJetCursor  cur;
	SDB sdb;
	String key,value;
	public IndexIterator(SDB sdb, String key, String value) throws SqlJetException {
		this.sdb=sdb;
		SqlJetTableHelper t = sdb.table(sdb.indexRecord);
		String value2=value+(char)32767;
		cur = t.scope(IndexRecord.NAME_VALUE_LAST_UPDATE, new Object[]{key,value,Long.MIN_VALUE},new Object[]{key,value2,Long.MIN_VALUE});
		this.key=key;
		this.value=value;
	}
	@Override
	public boolean hasNext() throws SqlJetException{
		return !cur.eof();
	}

	@Override
	public String toString() {
		return "(IndexIterator "+key+"="+value+")";
	}
	@Override
	public DocumentRecord next() throws SqlJetException{
		SqlJetRecord.fetch( sdb.indexRecord, cur);
		cur.next();
		DocumentRecord d = sdb.byId(sdb.indexRecord.document);
		return d;
	}
	@Override
	public void close() throws SqlJetException {
		cur.close();
	}

}
