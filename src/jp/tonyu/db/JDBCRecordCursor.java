package jp.tonyu.db;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

import jp.tonyu.debug.Log;



public class JDBCRecordCursor<T extends JDBCRecord> implements ResultSet,Iterable<T> {
	ResultSet cur;
	T record;
	@Override
	public Iterator<T> iterator() {
	    return new Iterator<T>() {

            @Override
            public boolean hasNext() {
                try {
                    boolean res=JDBCRecordCursor.this.next();
                    if (!res) {
                        close();
                    }
                    return res;
                } catch (SQLException e) {
                    e.printStackTrace();
                    Log.e(e);
                    return false;
                }
            }

            @Override
            public T next() {
                try {
                    return fetch();
                } catch (SQLException e) {
                    e.printStackTrace();
                    Log.e(e);
                    return null;
                }
            }

            @Override
            public void remove() {

            }
        };
	}
	public JDBCRecordCursor(T record, ResultSet cur) {
		this.cur=cur;
		this.record=record;
	}
	public T fetch() throws SQLException {
		T res = record.dup(record);
		JDBCHelper.fetch(res,cur);
		return res;
	}
	public boolean absolute(int row) throws SQLException {
		return cur.absolute(row);
	}
	public void afterLast() throws SQLException {
		cur.afterLast();
	}
	public void beforeFirst() throws SQLException {
		cur.beforeFirst();
	}
	public void cancelRowUpdates() throws SQLException {
		cur.cancelRowUpdates();
	}
	public void clearWarnings() throws SQLException {
		cur.clearWarnings();
	}
	public void close() throws SQLException {
	    Log.d(this, "Query closed!");
		cur.close();
	}
	public void deleteRow() throws SQLException {
		cur.deleteRow();
	}
	public int findColumn(String columnLabel) throws SQLException {
		return cur.findColumn(columnLabel);
	}
	public boolean first() throws SQLException {
		return cur.first();
	}
	public Array getArray(int columnIndex) throws SQLException {
		return cur.getArray(columnIndex);
	}
	public Array getArray(String columnLabel) throws SQLException {
		return cur.getArray(columnLabel);
	}
	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		return cur.getAsciiStream(columnIndex);
	}
	public InputStream getAsciiStream(String columnLabel) throws SQLException {
		return cur.getAsciiStream(columnLabel);
	}
	public BigDecimal getBigDecimal(int columnIndex, int scale)
			throws SQLException {
		return cur.getBigDecimal(columnIndex, scale);
	}
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		return cur.getBigDecimal(columnIndex);
	}
	public BigDecimal getBigDecimal(String columnLabel, int scale)
			throws SQLException {
		return cur.getBigDecimal(columnLabel, scale);
	}
	public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
		return cur.getBigDecimal(columnLabel);
	}
	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		return cur.getBinaryStream(columnIndex);
	}
	public InputStream getBinaryStream(String columnLabel) throws SQLException {
		return cur.getBinaryStream(columnLabel);
	}
	public Blob getBlob(int columnIndex) throws SQLException {
		return cur.getBlob(columnIndex);
	}
	public Blob getBlob(String columnLabel) throws SQLException {
		return cur.getBlob(columnLabel);
	}
	public boolean getBoolean(int columnIndex) throws SQLException {
		return cur.getBoolean(columnIndex);
	}
	public boolean getBoolean(String columnLabel) throws SQLException {
		return cur.getBoolean(columnLabel);
	}
	public byte getByte(int columnIndex) throws SQLException {
		return cur.getByte(columnIndex);
	}
	public byte getByte(String columnLabel) throws SQLException {
		return cur.getByte(columnLabel);
	}
	public byte[] getBytes(int columnIndex) throws SQLException {
		return cur.getBytes(columnIndex);
	}
	public byte[] getBytes(String columnLabel) throws SQLException {
		return cur.getBytes(columnLabel);
	}
	public Reader getCharacterStream(int columnIndex) throws SQLException {
		return cur.getCharacterStream(columnIndex);
	}
	public Reader getCharacterStream(String columnLabel) throws SQLException {
		return cur.getCharacterStream(columnLabel);
	}
	public Clob getClob(int columnIndex) throws SQLException {
		return cur.getClob(columnIndex);
	}
	public Clob getClob(String columnLabel) throws SQLException {
		return cur.getClob(columnLabel);
	}
	public int getConcurrency() throws SQLException {
		return cur.getConcurrency();
	}
	public String getCursorName() throws SQLException {
		return cur.getCursorName();
	}
	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		return cur.getDate(columnIndex, cal);
	}
	public Date getDate(int columnIndex) throws SQLException {
		return cur.getDate(columnIndex);
	}
	public Date getDate(String columnLabel, Calendar cal) throws SQLException {
		return cur.getDate(columnLabel, cal);
	}
	public Date getDate(String columnLabel) throws SQLException {
		return cur.getDate(columnLabel);
	}
	public double getDouble(int columnIndex) throws SQLException {
		return cur.getDouble(columnIndex);
	}
	public double getDouble(String columnLabel) throws SQLException {
		return cur.getDouble(columnLabel);
	}
	public int getFetchDirection() throws SQLException {
		return cur.getFetchDirection();
	}
	public int getFetchSize() throws SQLException {
		return cur.getFetchSize();
	}
	public float getFloat(int columnIndex) throws SQLException {
		return cur.getFloat(columnIndex);
	}
	public float getFloat(String columnLabel) throws SQLException {
		return cur.getFloat(columnLabel);
	}
	public int getHoldability() throws SQLException {
		return cur.getHoldability();
	}
	public int getInt(int columnIndex) throws SQLException {
		return cur.getInt(columnIndex);
	}
	public int getInt(String columnLabel) throws SQLException {
		return cur.getInt(columnLabel);
	}
	public long getLong(int columnIndex) throws SQLException {
		return cur.getLong(columnIndex);
	}
	public long getLong(String columnLabel) throws SQLException {
		return cur.getLong(columnLabel);
	}
	public ResultSetMetaData getMetaData() throws SQLException {
		return cur.getMetaData();
	}
	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		return cur.getNCharacterStream(columnIndex);
	}
	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		return cur.getNCharacterStream(columnLabel);
	}
	public NClob getNClob(int columnIndex) throws SQLException {
		return cur.getNClob(columnIndex);
	}
	public NClob getNClob(String columnLabel) throws SQLException {
		return cur.getNClob(columnLabel);
	}
	public String getNString(int columnIndex) throws SQLException {
		return cur.getNString(columnIndex);
	}
	public String getNString(String columnLabel) throws SQLException {
		return cur.getNString(columnLabel);
	}
	public Object getObject(int columnIndex, Map<String, Class<?>> map)
			throws SQLException {
		return cur.getObject(columnIndex, map);
	}
	public Object getObject(int columnIndex) throws SQLException {
		return cur.getObject(columnIndex);
	}
	public Object getObject(String columnLabel, Map<String, Class<?>> map)
			throws SQLException {
		return cur.getObject(columnLabel, map);
	}
	public Object getObject(String columnLabel) throws SQLException {
		return cur.getObject(columnLabel);
	}
	public Ref getRef(int columnIndex) throws SQLException {
		return cur.getRef(columnIndex);
	}
	public Ref getRef(String columnLabel) throws SQLException {
		return cur.getRef(columnLabel);
	}
	public int getRow() throws SQLException {
		return cur.getRow();
	}
	public RowId getRowId(int columnIndex) throws SQLException {
		return cur.getRowId(columnIndex);
	}
	public RowId getRowId(String columnLabel) throws SQLException {
		return cur.getRowId(columnLabel);
	}
	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		return cur.getSQLXML(columnIndex);
	}
	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		return cur.getSQLXML(columnLabel);
	}
	public short getShort(int columnIndex) throws SQLException {
		return cur.getShort(columnIndex);
	}
	public short getShort(String columnLabel) throws SQLException {
		return cur.getShort(columnLabel);
	}
	public Statement getStatement() throws SQLException {
		return cur.getStatement();
	}
	public String getString(int columnIndex) throws SQLException {
		return cur.getString(columnIndex);
	}
	public String getString(String columnLabel) throws SQLException {
		return cur.getString(columnLabel);
	}
	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		return cur.getTime(columnIndex, cal);
	}
	public Time getTime(int columnIndex) throws SQLException {
		return cur.getTime(columnIndex);
	}
	public Time getTime(String columnLabel, Calendar cal) throws SQLException {
		return cur.getTime(columnLabel, cal);
	}
	public Time getTime(String columnLabel) throws SQLException {
		return cur.getTime(columnLabel);
	}
	public Timestamp getTimestamp(int columnIndex, Calendar cal)
			throws SQLException {
		return cur.getTimestamp(columnIndex, cal);
	}
	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		return cur.getTimestamp(columnIndex);
	}
	public Timestamp getTimestamp(String columnLabel, Calendar cal)
			throws SQLException {
		return cur.getTimestamp(columnLabel, cal);
	}
	public Timestamp getTimestamp(String columnLabel) throws SQLException {
		return cur.getTimestamp(columnLabel);
	}
	public int getType() throws SQLException {
		return cur.getType();
	}
	public URL getURL(int columnIndex) throws SQLException {
		return cur.getURL(columnIndex);
	}
	public URL getURL(String columnLabel) throws SQLException {
		return cur.getURL(columnLabel);
	}
	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		return cur.getUnicodeStream(columnIndex);
	}
	public InputStream getUnicodeStream(String columnLabel) throws SQLException {
		return cur.getUnicodeStream(columnLabel);
	}
	public SQLWarning getWarnings() throws SQLException {
		return cur.getWarnings();
	}
	public void insertRow() throws SQLException {
		cur.insertRow();
	}
	public boolean isAfterLast() throws SQLException {
		return cur.isAfterLast();
	}
	public boolean isBeforeFirst() throws SQLException {
		return cur.isBeforeFirst();
	}
	public boolean isClosed() throws SQLException {
		return cur.isClosed();
	}
	public boolean isFirst() throws SQLException {
		return cur.isFirst();
	}
	public boolean isLast() throws SQLException {
		return cur.isLast();
	}
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		return cur.isWrapperFor(arg0);
	}
	public boolean last() throws SQLException {
		return cur.last();
	}
	public void moveToCurrentRow() throws SQLException {
		cur.moveToCurrentRow();
	}
	public void moveToInsertRow() throws SQLException {
		cur.moveToInsertRow();
	}
	public boolean next() throws SQLException {
		return cur.next();
	}
	public boolean previous() throws SQLException {
		return cur.previous();
	}
	public void refreshRow() throws SQLException {
		cur.refreshRow();
	}
	public boolean relative(int rows) throws SQLException {
		return cur.relative(rows);
	}
	public boolean rowDeleted() throws SQLException {
		return cur.rowDeleted();
	}
	public boolean rowInserted() throws SQLException {
		return cur.rowInserted();
	}
	public boolean rowUpdated() throws SQLException {
		return cur.rowUpdated();
	}
	public void setFetchDirection(int direction) throws SQLException {
		cur.setFetchDirection(direction);
	}
	public void setFetchSize(int rows) throws SQLException {
		cur.setFetchSize(rows);
	}
	public <T> T unwrap(Class<T> arg0) throws SQLException {
		return cur.unwrap(arg0);
	}
	public void updateArray(int columnIndex, Array x) throws SQLException {
		cur.updateArray(columnIndex, x);
	}
	public void updateArray(String columnLabel, Array x) throws SQLException {
		cur.updateArray(columnLabel, x);
	}
	public void updateAsciiStream(int columnIndex, InputStream x, int length)
			throws SQLException {
		cur.updateAsciiStream(columnIndex, x, length);
	}
	public void updateAsciiStream(int columnIndex, InputStream x, long length)
			throws SQLException {
		cur.updateAsciiStream(columnIndex, x, length);
	}
	public void updateAsciiStream(int columnIndex, InputStream x)
			throws SQLException {
		cur.updateAsciiStream(columnIndex, x);
	}
	public void updateAsciiStream(String columnLabel, InputStream x, int length)
			throws SQLException {
		cur.updateAsciiStream(columnLabel, x, length);
	}
	public void updateAsciiStream(String columnLabel, InputStream x, long length)
			throws SQLException {
		cur.updateAsciiStream(columnLabel, x, length);
	}
	public void updateAsciiStream(String columnLabel, InputStream x)
			throws SQLException {
		cur.updateAsciiStream(columnLabel, x);
	}
	public void updateBigDecimal(int columnIndex, BigDecimal x)
			throws SQLException {
		cur.updateBigDecimal(columnIndex, x);
	}
	public void updateBigDecimal(String columnLabel, BigDecimal x)
			throws SQLException {
		cur.updateBigDecimal(columnLabel, x);
	}
	public void updateBinaryStream(int columnIndex, InputStream x, int length)
			throws SQLException {
		cur.updateBinaryStream(columnIndex, x, length);
	}
	public void updateBinaryStream(int columnIndex, InputStream x, long length)
			throws SQLException {
		cur.updateBinaryStream(columnIndex, x, length);
	}
	public void updateBinaryStream(int columnIndex, InputStream x)
			throws SQLException {
		cur.updateBinaryStream(columnIndex, x);
	}
	public void updateBinaryStream(String columnLabel, InputStream x, int length)
			throws SQLException {
		cur.updateBinaryStream(columnLabel, x, length);
	}
	public void updateBinaryStream(String columnLabel, InputStream x,
			long length) throws SQLException {
		cur.updateBinaryStream(columnLabel, x, length);
	}
	public void updateBinaryStream(String columnLabel, InputStream x)
			throws SQLException {
		cur.updateBinaryStream(columnLabel, x);
	}
	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		cur.updateBlob(columnIndex, x);
	}
	public void updateBlob(int columnIndex, InputStream inputStream, long length)
			throws SQLException {
		cur.updateBlob(columnIndex, inputStream, length);
	}
	public void updateBlob(int columnIndex, InputStream inputStream)
			throws SQLException {
		cur.updateBlob(columnIndex, inputStream);
	}
	public void updateBlob(String columnLabel, Blob x) throws SQLException {
		cur.updateBlob(columnLabel, x);
	}
	public void updateBlob(String columnLabel, InputStream inputStream,
			long length) throws SQLException {
		cur.updateBlob(columnLabel, inputStream, length);
	}
	public void updateBlob(String columnLabel, InputStream inputStream)
			throws SQLException {
		cur.updateBlob(columnLabel, inputStream);
	}
	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		cur.updateBoolean(columnIndex, x);
	}
	public void updateBoolean(String columnLabel, boolean x)
			throws SQLException {
		cur.updateBoolean(columnLabel, x);
	}
	public void updateByte(int columnIndex, byte x) throws SQLException {
		cur.updateByte(columnIndex, x);
	}
	public void updateByte(String columnLabel, byte x) throws SQLException {
		cur.updateByte(columnLabel, x);
	}
	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		cur.updateBytes(columnIndex, x);
	}
	public void updateBytes(String columnLabel, byte[] x) throws SQLException {
		cur.updateBytes(columnLabel, x);
	}
	public void updateCharacterStream(int columnIndex, Reader x, int length)
			throws SQLException {
		cur.updateCharacterStream(columnIndex, x, length);
	}
	public void updateCharacterStream(int columnIndex, Reader x, long length)
			throws SQLException {
		cur.updateCharacterStream(columnIndex, x, length);
	}
	public void updateCharacterStream(int columnIndex, Reader x)
			throws SQLException {
		cur.updateCharacterStream(columnIndex, x);
	}
	public void updateCharacterStream(String columnLabel, Reader reader,
			int length) throws SQLException {
		cur.updateCharacterStream(columnLabel, reader, length);
	}
	public void updateCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException {
		cur.updateCharacterStream(columnLabel, reader, length);
	}
	public void updateCharacterStream(String columnLabel, Reader reader)
			throws SQLException {
		cur.updateCharacterStream(columnLabel, reader);
	}
	public void updateClob(int columnIndex, Clob x) throws SQLException {
		cur.updateClob(columnIndex, x);
	}
	public void updateClob(int columnIndex, Reader reader, long length)
			throws SQLException {
		cur.updateClob(columnIndex, reader, length);
	}
	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		cur.updateClob(columnIndex, reader);
	}
	public void updateClob(String columnLabel, Clob x) throws SQLException {
		cur.updateClob(columnLabel, x);
	}
	public void updateClob(String columnLabel, Reader reader, long length)
			throws SQLException {
		cur.updateClob(columnLabel, reader, length);
	}
	public void updateClob(String columnLabel, Reader reader)
			throws SQLException {
		cur.updateClob(columnLabel, reader);
	}
	public void updateDate(int columnIndex, Date x) throws SQLException {
		cur.updateDate(columnIndex, x);
	}
	public void updateDate(String columnLabel, Date x) throws SQLException {
		cur.updateDate(columnLabel, x);
	}
	public void updateDouble(int columnIndex, double x) throws SQLException {
		cur.updateDouble(columnIndex, x);
	}
	public void updateDouble(String columnLabel, double x) throws SQLException {
		cur.updateDouble(columnLabel, x);
	}
	public void updateFloat(int columnIndex, float x) throws SQLException {
		cur.updateFloat(columnIndex, x);
	}
	public void updateFloat(String columnLabel, float x) throws SQLException {
		cur.updateFloat(columnLabel, x);
	}
	public void updateInt(int columnIndex, int x) throws SQLException {
		cur.updateInt(columnIndex, x);
	}
	public void updateInt(String columnLabel, int x) throws SQLException {
		cur.updateInt(columnLabel, x);
	}
	public void updateLong(int columnIndex, long x) throws SQLException {
		cur.updateLong(columnIndex, x);
	}
	public void updateLong(String columnLabel, long x) throws SQLException {
		cur.updateLong(columnLabel, x);
	}
	public void updateNCharacterStream(int columnIndex, Reader x, long length)
			throws SQLException {
		cur.updateNCharacterStream(columnIndex, x, length);
	}
	public void updateNCharacterStream(int columnIndex, Reader x)
			throws SQLException {
		cur.updateNCharacterStream(columnIndex, x);
	}
	public void updateNCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException {
		cur.updateNCharacterStream(columnLabel, reader, length);
	}
	public void updateNCharacterStream(String columnLabel, Reader reader)
			throws SQLException {
		cur.updateNCharacterStream(columnLabel, reader);
	}
	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
		cur.updateNClob(columnIndex, nClob);
	}
	public void updateNClob(int columnIndex, Reader reader, long length)
			throws SQLException {
		cur.updateNClob(columnIndex, reader, length);
	}
	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		cur.updateNClob(columnIndex, reader);
	}
	public void updateNClob(String columnLabel, NClob nClob)
			throws SQLException {
		cur.updateNClob(columnLabel, nClob);
	}
	public void updateNClob(String columnLabel, Reader reader, long length)
			throws SQLException {
		cur.updateNClob(columnLabel, reader, length);
	}
	public void updateNClob(String columnLabel, Reader reader)
			throws SQLException {
		cur.updateNClob(columnLabel, reader);
	}
	public void updateNString(int columnIndex, String nString)
			throws SQLException {
		cur.updateNString(columnIndex, nString);
	}
	public void updateNString(String columnLabel, String nString)
			throws SQLException {
		cur.updateNString(columnLabel, nString);
	}
	public void updateNull(int columnIndex) throws SQLException {
		cur.updateNull(columnIndex);
	}
	public void updateNull(String columnLabel) throws SQLException {
		cur.updateNull(columnLabel);
	}
	public void updateObject(int columnIndex, Object x, int scaleOrLength)
			throws SQLException {
		cur.updateObject(columnIndex, x, scaleOrLength);
	}
	public void updateObject(int columnIndex, Object x) throws SQLException {
		cur.updateObject(columnIndex, x);
	}
	public void updateObject(String columnLabel, Object x, int scaleOrLength)
			throws SQLException {
		cur.updateObject(columnLabel, x, scaleOrLength);
	}
	public void updateObject(String columnLabel, Object x) throws SQLException {
		cur.updateObject(columnLabel, x);
	}
	public void updateRef(int columnIndex, Ref x) throws SQLException {
		cur.updateRef(columnIndex, x);
	}
	public void updateRef(String columnLabel, Ref x) throws SQLException {
		cur.updateRef(columnLabel, x);
	}
	public void updateRow() throws SQLException {
		cur.updateRow();
	}
	public void updateRowId(int columnIndex, RowId x) throws SQLException {
		cur.updateRowId(columnIndex, x);
	}
	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		cur.updateRowId(columnLabel, x);
	}
	public void updateSQLXML(int columnIndex, SQLXML xmlObject)
			throws SQLException {
		cur.updateSQLXML(columnIndex, xmlObject);
	}
	public void updateSQLXML(String columnLabel, SQLXML xmlObject)
			throws SQLException {
		cur.updateSQLXML(columnLabel, xmlObject);
	}
	public void updateShort(int columnIndex, short x) throws SQLException {
		cur.updateShort(columnIndex, x);
	}
	public void updateShort(String columnLabel, short x) throws SQLException {
		cur.updateShort(columnLabel, x);
	}
	public void updateString(int columnIndex, String x) throws SQLException {
		cur.updateString(columnIndex, x);
	}
	public void updateString(String columnLabel, String x) throws SQLException {
		cur.updateString(columnLabel, x);
	}
	public void updateTime(int columnIndex, Time x) throws SQLException {
		cur.updateTime(columnIndex, x);
	}
	public void updateTime(String columnLabel, Time x) throws SQLException {
		cur.updateTime(columnLabel, x);
	}
	public void updateTimestamp(int columnIndex, Timestamp x)
			throws SQLException {
		cur.updateTimestamp(columnIndex, x);
	}
	public void updateTimestamp(String columnLabel, Timestamp x)
			throws SQLException {
		cur.updateTimestamp(columnLabel, x);
	}
	public boolean wasNull() throws SQLException {
		return cur.wasNull();
	}


}
