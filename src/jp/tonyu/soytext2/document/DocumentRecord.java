package jp.tonyu.soytext2.document;

import org.tmatesoft.sqljet.core.SqlJetException;

import jp.tonyu.db.SqlJetHelper;
import jp.tonyu.db.SqlJetRecord;
import jp.tonyu.db.SqlJetTableHelper;
import jp.tonyu.util.Ref;


public class DocumentRecord extends SqlJetRecord /*implements Wrappable*/ {
	/*public DocumentRecord(SqlJetHelper db) {
		super(db);
	}*/
	
	/*	db.createTable("CREATE TABLE "+DOCUMENT_1+"(\n"+
			    "   id TEXT NOT NULL PRIMARY KEY,\n"+
			    "   lastupdate INTEGER NOT NULL,\n"+
			    "   createdate INTEGER NOT NULL, \n"+
			    "   lastaccessed INTEGER NOT NULL,\n"+
			    "   language TEXT,\n"+
			    "   summary TEXT,\n"+
			    "   precontent TEXT,\n"+
			    "   content TEXT,\n"+
			    "   owner TEXT,\n"+
			    "   group TEXT,\n"+
			    "   permission TEXT \n"+
			    ")\n"+
			    "");*/
	@Override
	public String[] fieldOrder() {
		return new String[]{"id","lastUpdate","createDate","lastAccessed","language",
				"summary","preContent","content","owner","group","permission"
		};
	}
	@Override
	public String tableName() {
		return "DocumentRecord";
	}
	//private DocumentSet documentSet;
	public String id;
	public long lastUpdate,createDate,lastAccessed;
	public String summary,content,preContent;
	public String language="javascript";
	public String owner="",group="",permission="";
	/*public DocumentRecord(String id) {
		//this.documentSet=documentSet;
		this.id=id;
	}*/
	@Override
	public String toString() {
		return "(Document "+id+")";
	}
	@Override
	public String[] indexNames() {
		return q("lastUpdate","lastAccessed","owner");
	}
	/*public void save() {
		documentSet.save(this);
	}*/
	public void insertTo(SqlJetTableHelper t) throws SqlJetException {
		insertTo(t.table());
	}
	
}
