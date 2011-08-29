package jp.tonyu.soytext2.document;

import jp.tonyu.db.SqlJetHelper;
import jp.tonyu.db.SqlJetRecord;

public class UIDRecord extends SqlJetRecord {
	/*public UIDRecord(SqlJetHelper db) {
		super(db);
	}*/
	public String id;
	@Override
	public String tableName() {
		return "UIDRecord";
	}

}
