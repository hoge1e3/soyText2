package jp.tonyu.soytext2.document;

public class LogRecord {
	/*static String schema="CREATE TABLE "+LOG_1+" (\n"+
    "   id INTEGER NOT NULL PRIMARY KEY,\n"+
    "   date TEXT NOT NULL,\n"+
    "   action TEXT,\n"+
    "   target TEXT,\n"+
    "   option TEXT\n"+
    ")\n"+
    "";*/
	public boolean inDB=false;
	public int id;
	public String date;
	public String action,target,option;
	public LogRecord(int id) {
		super();
		this.id = id;
	}
	
}
