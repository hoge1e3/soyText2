package jp.tonyu.soytext2.document;

import jp.tonyu.db.JDBCRecord;

public class LogRecord extends JDBCRecord {
	/*public LogRecord(SqlJetHelper db) {
		super(db);
		// TODO Auto-generated constructor stub
	}*/

	/*static String schema="CREATE TABLE "+LOG_1+" (\n"+
    "   id INTEGER NOT NULL PRIMARY KEY,\n"+
    "   date TEXT NOT NULL,\n"+
    "   action TEXT,\n"+
    "   target TEXT,\n"+
    "   option TEXT\n"+
    ")\n"+
    "";*/
	boolean inDB=false;
	public int id;
	public String date;
	public String action,target,option;
	@Override
	public String[] columnOrder() {
		return new String[]{"id","date","action","target","option"};
	}
	@Override
	public String tableName() {
		return "LogRecord";
	}

	/*public LogRecord(int id) {
		super();
		this.id = id;
	}*/
	public static LogRecord create(int id) {
		LogRecord res = new LogRecord();
		res.id=id;
		return res;
	}


}
