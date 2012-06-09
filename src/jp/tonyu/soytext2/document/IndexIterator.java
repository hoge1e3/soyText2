package jp.tonyu.soytext2.document;

import java.sql.SQLException;

/**
 * DocumentRecords must be iterated in order newer->older (lastUpdate desc)
 * Except  name index ...
 * @author shinya
 *
 */
public interface IndexIterator {
	public boolean hasNext() throws SQLException;
	public IndexRecord next() throws SQLException;
	public void close() throws SQLException;
}
