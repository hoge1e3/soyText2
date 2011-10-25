package jp.tonyu.soytext2.document;

import jp.tonyu.db.SqlJetRecord;

public class EnvRecord extends SqlJetRecord {
	public String id,value;
	@Override
	public String tableName() {
		return "EnvRecord";
	}

}
