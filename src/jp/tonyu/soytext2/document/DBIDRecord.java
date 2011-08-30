package jp.tonyu.soytext2.document;

import jp.tonyu.db.SqlJetRecord;

public class DBIDRecord extends SqlJetRecord {
	/*public UIDRecord(SqlJetHelper db) {
		super(db);
	}*/
	public String id;
	@Override
	public String tableName() {
		return "DBIDRecord";
	}

}
