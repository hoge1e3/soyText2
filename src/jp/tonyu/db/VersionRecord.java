package jp.tonyu.db;

public class VersionRecord extends JDBCRecord {
	public int id;
	public int version;
	@Override
	public String tableName() {
		// TODO 自動生成されたメソッド・スタブ
		return JDBCHelper.VERSION_TABLE;
	}
}
