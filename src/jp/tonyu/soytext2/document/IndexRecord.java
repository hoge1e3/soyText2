package jp.tonyu.soytext2.document;

import jp.tonyu.db.SqlJetRecord;

public class IndexRecord extends SqlJetRecord {
	public static final String NAME_VALUE_LAST_UPDATE = "name,value,lastUpdate";
	public int id;
	public String document,name,value;
	public long lastUpdate;
	// value = "s{String}";  value="d{ID}";  
	public static final String DEFINED_INDEX_NAMES="#DEFINED_INDEX_NAMES";
	public static final String INDEX_REFERS="#REFERS";
	public static final String INDEX_INSTANCEOF="#INSTANCEOF";

	//public static final boolean useIndex=false;
	public IndexRecord() {
		super();
	}
	@Override
	public String tableName() {
		return "IndexRecord";
	}
	@Override
	public String[] indexNames() {
		return q(NAME_VALUE_LAST_UPDATE,"document"); 
		// "document" needs on removing indexes, removing needed on updating document
	}

}
