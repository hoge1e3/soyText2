package jp.tonyu.soytext2.document;

import org.tmatesoft.sqljet.core.SqlJetException;

/**
 * DocumentRecords must be iterated in order newer->older (lastUpdate desc)
 * Except  name index ...
 * @author shinya
 *
 */
public interface DocumentRecordIterator {
	public boolean hasNext() throws SqlJetException;
	public DocumentRecord next() throws SqlJetException;
	public void close() throws SqlJetException;
}
