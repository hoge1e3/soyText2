package jp.tonyu.db;

import java.io.InputStream;
import java.util.Map;

import jp.tonyu.soytext2.document.DocumentRecord;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetValueType;
import org.tmatesoft.sqljet.core.schema.SqlJetConflictAction;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

public class SqlJetRecordCursor<T extends SqlJetRecord> implements ISqlJetCursor {
	ISqlJetCursor cur;
	T record;
	public SqlJetRecordCursor(T record, ISqlJetCursor cur) {
		this.cur=cur;
		this.record=record;
	}
	public T fetch() throws SqlJetException {
		T res = record.dup(record);
		SqlJetRecord.fetch(res,cur);
		return res;
	}
	public void close() throws SqlJetException {
		cur.close();
	}

	public void delete() throws SqlJetException {
		cur.delete();
	}

	public boolean eof() throws SqlJetException {
		return cur.eof();
	}

	public boolean first() throws SqlJetException {
		return cur.first();
	}

	public byte[] getBlobAsArray(int arg0) throws SqlJetException {
		return cur.getBlobAsArray(arg0);
	}

	public byte[] getBlobAsArray(String arg0) throws SqlJetException {
		return cur.getBlobAsArray(arg0);
	}

	public InputStream getBlobAsStream(int arg0) throws SqlJetException {
		return cur.getBlobAsStream(arg0);
	}

	public InputStream getBlobAsStream(String arg0) throws SqlJetException {
		return cur.getBlobAsStream(arg0);
	}

	public boolean getBoolean(int arg0) throws SqlJetException {
		return cur.getBoolean(arg0);
	}

	public boolean getBoolean(String arg0) throws SqlJetException {
		return cur.getBoolean(arg0);
	}

	public SqlJetValueType getFieldType(int arg0) throws SqlJetException {
		return cur.getFieldType(arg0);
	}

	public SqlJetValueType getFieldType(String arg0) throws SqlJetException {
		return cur.getFieldType(arg0);
	}

	public int getFieldsCount() throws SqlJetException {
		return cur.getFieldsCount();
	}

	public double getFloat(int arg0) throws SqlJetException {
		return cur.getFloat(arg0);
	}

	public double getFloat(String arg0) throws SqlJetException {
		return cur.getFloat(arg0);
	}

	public long getInteger(int arg0) throws SqlJetException {
		return cur.getInteger(arg0);
	}

	public long getInteger(String arg0) throws SqlJetException {
		return cur.getInteger(arg0);
	}

	public long getLimit() {
		return cur.getLimit();
	}

	public long getRowCount() throws SqlJetException {
		return cur.getRowCount();
	}

	public long getRowId() throws SqlJetException {
		return cur.getRowId();
	}

	public long getRowIndex() throws SqlJetException {
		return cur.getRowIndex();
	}

	public String getString(int arg0) throws SqlJetException {
		return cur.getString(arg0);
	}

	public String getString(String arg0) throws SqlJetException {
		return cur.getString(arg0);
	}

	public Object getValue(int arg0) throws SqlJetException {
		return cur.getValue(arg0);
	}

	public Object getValue(String arg0) throws SqlJetException {
		return cur.getValue(arg0);
	}

	public boolean goTo(long arg0) throws SqlJetException {
		return cur.goTo(arg0);
	}

	public boolean goToRow(long arg0) throws SqlJetException {
		return cur.goToRow(arg0);
	}

	public boolean isNull(int arg0) throws SqlJetException {
		return cur.isNull(arg0);
	}

	public boolean isNull(String arg0) throws SqlJetException {
		return cur.isNull(arg0);
	}

	public boolean last() throws SqlJetException {
		return cur.last();
	}

	public boolean next() throws SqlJetException {
		return cur.next();
	}

	public boolean previous() throws SqlJetException {
		return cur.previous();
	}

	public ISqlJetCursor reverse() throws SqlJetException {
		return cur.reverse();
	}

	public void setLimit(long arg0) throws SqlJetException {
		cur.setLimit(arg0);
	}

	public void update(Object... arg0) throws SqlJetException {
		cur.update(arg0);
	}

	public void updateByFieldNames(Map<String, Object> arg0)
			throws SqlJetException {
		cur.updateByFieldNames(arg0);
	}

	public void updateByFieldNamesOr(SqlJetConflictAction arg0,
			Map<String, Object> arg1) throws SqlJetException {
		cur.updateByFieldNamesOr(arg0, arg1);
	}

	public void updateOr(SqlJetConflictAction arg0, Object... arg1)
			throws SqlJetException {
		cur.updateOr(arg0, arg1);
	}

	public long updateWithRowId(long arg0, Object... arg1)
			throws SqlJetException {
		return cur.updateWithRowId(arg0, arg1);
	}

	public long updateWithRowIdOr(SqlJetConflictAction arg0, long arg1,
			Object... arg2) throws SqlJetException {
		return cur.updateWithRowIdOr(arg0, arg1, arg2);
	}
	public SqlJetRecordCursor<T> reverseRecordCursor() throws SqlJetException {
		return new SqlJetRecordCursor<T>(record, cur.reverse());
	}

}
