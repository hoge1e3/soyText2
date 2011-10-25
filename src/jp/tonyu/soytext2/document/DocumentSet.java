package jp.tonyu.soytext2.document;

import java.util.Map;

public interface DocumentSet {
	public DocumentRecord newDocument();
	public DocumentRecord newDocument(String id);
	public void save(DocumentRecord d, Map<String, String> updatingIndex);
	public void updateIndex(DocumentRecord d,Map<String, String> h);
	public DocumentRecord byId(String id);
	public void all(DocumentAction a);
	public int log( String date, String action, String target, String option);
}
