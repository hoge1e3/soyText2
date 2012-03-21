package jp.tonyu.soytext2.document;

import java.util.Iterator;

import jp.tonyu.db.SqlJetTableHelper;
import jp.tonyu.soytext2.js.DocumentScriptable;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

public class IndexIterator implements Iterator<DocumentRecord> {
	ISqlJetCursor  cur;
	SDB sdb;
	public IndexIterator(SDB sdb, String key, String value) throws SqlJetException {
		this.sdb=sdb;
		SqlJetTableHelper t = sdb.table(sdb.indexRecord);
		String value2=value+(char)32767;
		cur = t.scope(IndexRecord.NAME_VALUE_LAST_UPDATE, new Object[]{key,value,Long.MIN_VALUE},new Object[]{key,value2,Long.MIN_VALUE});
	}
	@Override
	public boolean hasNext() {
		try {
			return !cur.eof();
		} catch (SqlJetException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public DocumentRecord next() {
		try {
			sdb.indexRecord.fetch(cur);
			cur.next();
			DocumentRecord d = sdb.byId(sdb.indexRecord.document);
			return d;
		} catch (SqlJetException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void remove() {
	}

}
