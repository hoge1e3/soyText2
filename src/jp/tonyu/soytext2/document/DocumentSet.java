package jp.tonyu.soytext2.document;

public interface DocumentSet {
	public DocumentRecord newDocument();
	public DocumentRecord newDocument(String id);
	public void save(DocumentRecord d);
	public DocumentRecord byId(String id);
	public void all(DocumentAction a);
}
