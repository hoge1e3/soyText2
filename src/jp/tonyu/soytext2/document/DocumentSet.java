package jp.tonyu.soytext2.document;

public interface DocumentSet {
	public Document newDocument();
	public Document newDocument(String id);
	public void save(Document d);
	public Document byId(String id);
	public void all(DocumentAction a);
}
