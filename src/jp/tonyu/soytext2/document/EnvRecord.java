package jp.tonyu.soytext2.document;

import jp.tonyu.db.JDBCRecord;

public class EnvRecord extends JDBCRecord {
	public String id,value;
	@Override
	public String tableName() {
		return "EnvRecord";
	}

}
