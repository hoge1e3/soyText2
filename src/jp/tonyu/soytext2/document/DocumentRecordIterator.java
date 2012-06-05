package jp.tonyu.soytext2.document;

import java.sql.SQLException;

/**
 * DocumentRecords must be iterated in order newer->older (lastUpdate desc)
 * Except  name index ...
 * @author shinya
 *
 */
public interface DocumentRecordIterator {
	public boolean hasNext() throws SQLException;
	public DocumentRecord next() throws SQLException;
	public void close() throws SQLException;
}
