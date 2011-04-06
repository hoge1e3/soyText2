package jp.tonyu.soytext2.document;

public class Document {
	/*"CREATE TABLE "+DOCUMENT_1+"(\n"+
		    "   id TEXT NOT NULL PRIMARY KEY,\n"+
		    "   lastupdate INTEGER NOT NULL,\n"+
		    "   createdate INTEGER NOT NULL, \n"+
		    "   lastaccessed INTEGER NOT NULL,\n"+
		    "   summary TEXT,\n"+
		    "   content TEXT,\n"+
		    "   owner TEXT,\n"+
		    "   group TEXT,\n"+
		    "   permission TEXT \n"+
		    ")\n"+
		    "");*/
	public String id;
	public long lastUpdate,createDate,lastAccessed;
	public String summary,content;
	public String owner,group,permission;
}
