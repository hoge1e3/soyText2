package jp.tonyu.soytext2.document;

import org.tmatesoft.sqljet.core.SqlJetException;

import jp.tonyu.db.SqlJetRecord;
import jp.tonyu.db.SqlJetTableHelper;

public class LogRecord extends SqlJetRecord {
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
	public String[] fieldOrder() {
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
