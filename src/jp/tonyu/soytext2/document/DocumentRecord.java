package jp.tonyu.soytext2.document;

import jp.tonyu.db.JDBCRecord;


public class DocumentRecord extends JDBCRecord /*implements Wrappable*/ {
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
	public String[] columnOrder() {
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
	public static final String OWNER="owner",LASTUPDATE="lastUpdate",LASTUPDATE_DESC="-lastUpdate";
	/*public DocumentRecord(String id) {
		//this.documentSet=documentSet;
		this.id=id;
	}*/
	@Override
	public String toString() {
		return "(Document "+id+")";
	}
	@Override
	public String[] indexSpecs() {
		return q(LASTUPDATE_DESC,"lastAccessed",OWNER+","+LASTUPDATE_DESC);
	}



	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public long getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	public long getCreateDate() {
		return createDate;
	}
	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	}
	public long getLastAccessed() {
		return lastAccessed;
	}
	public void setLastAccessed(long lastAccessed) {
		this.lastAccessed = lastAccessed;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getPermission() {
		return permission;
	}
	public void setPermission(String permission) {
		this.permission = permission;
	}


}
