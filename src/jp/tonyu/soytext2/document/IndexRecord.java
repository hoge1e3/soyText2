package jp.tonyu.soytext2.document;

import jp.tonyu.db.SqlJetRecord;

public class IndexRecord extends SqlJetRecord {
	public int id;
	public String document,name,value;
	public long lastUpdate;
	// value = "s{String}";  value="d{ID}";  
	public static String DEFINED_INDEX_NAMES="DEFINED_INDEX_NAMES";
	public static String INDEX_BACKREF="INDEX_BACKREF";
	public static final boolean useIndex=false;

	@Override
	public String tableName() {
		return "IndexRecord";
	}
	@Override
	public String[] indexNames() {
		return q("name,value,lastUpdate","document"); 
		// "document" needs on removing indexes, removing needed on updating document
	}

}
