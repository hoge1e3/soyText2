package jp.tonyu.soytext2.document;

import java.util.Map;

public interface DocumentSet {
	public DocumentRecord newDocument();
	public DocumentRecord newDocument(String id);
	public void save(DocumentRecord d, PairSet<String,String> updatingIndex);
	public void updateIndex(DocumentRecord d,PairSet<String,String> h);
	public DocumentRecord byId(String id);
	public void all(DocumentAction a);
	public void transaction(Object mode, Runnable action);
	public int log( String date, String action, String target, String option);
	public String getDBID();
	public void searchByIndex(String key, String value, IndexAction a);
	public void searchByIndex(Map<String, String> keyValues, IndexAction a);
	public boolean indexAvailable(String key);
}
