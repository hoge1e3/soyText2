package jp.tonyu.soytext2.document;

import jp.tonyu.db.SqlJetRecord;

public class RemoteRecord extends SqlJetRecord {
	public int id;
	/**
	 * Unique Database id like "1.2010.tonyu.jp"
	 */
	public String uid;
	/**
	 * an URL which serves data of uid
	 */
	public String url;
	/**
	 * an URL from which the information of this record is obtained.
	 * If null, this system "believes" that the information is true.
	 */
	public String statedBy;
	/**
	 * A LogRecord(of remote system) id when synced last. 
	 */
	public long lastSynced;
	@Override
	public String tableName() {
		return "RemoteRecord";
	}
}
