package jp.tonyu.soytext2.document;

import jp.tonyu.soytext2.js.Wrappable;

public class Document implements Wrappable {
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
	public final DocumentSet documentSet;
	public final String id;
	public long lastUpdate,createDate,lastAccessed;
	public String summary,content;
	public String owner,group,permission;
	public Document(DocumentSet documentSet,String id) {
		this.documentSet=documentSet;
		this.id=id;
	}
	@Override
	public String toString() {
		return "(Document "+id+")";
	}
	public void save() {
		documentSet.save(this);
	}
	
}
