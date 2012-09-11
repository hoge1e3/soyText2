package jp.tonyu.soytext2.document;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import jp.tonyu.db.NotInReadTransactionException;
import jp.tonyu.db.NotInWriteTransactionException;
import jp.tonyu.db.TransactionMode;
import jp.tonyu.soytext2.file.BinData;
import jp.tonyu.soytext2.file.ReadableBinData;

public interface DocumentSet {
	public DocumentRecord newDocument() throws NotInWriteTransactionException;
	public DocumentRecord newDocument(String id) throws NotInWriteTransactionException;
	public void save(DocumentRecord d, PairSet<String,String> updatingIndex) throws NotInWriteTransactionException;
	public void updateIndex(DocumentRecord d,PairSet<String,String> h) throws NotInWriteTransactionException;
	public DocumentRecord byId(String id) throws NotInReadTransactionException;
	public void transaction(TransactionMode mode);
    //public Object transactionMode();//, Runnable action);
	public File getBlob(String id);
	public HashBlob getHashBlob(String hash);
	public HashBlob writeHashBlob(InputStream i);
    public void commit();
    public void rollback();
	public int log( String date, String action, String target, String option)  throws NotInWriteTransactionException;
	public String getDBID();

	//public void searchByIndex(String key, String value, IndexAction a)  throws NotInReadTransactionException;
	public void searchByIndex(Map<String, String> keyValues, IndexAction a)  throws NotInReadTransactionException;
	public void all(DocumentAction a)  throws NotInReadTransactionException;
    public void searchByIndex(Map<String, String> keyValues, UpdatingIndexAction a)  throws NotInWriteTransactionException;
    public void all(UpdatingDocumentAction a)  throws NotInWriteTransactionException;

    public boolean indexAvailable(String key)  throws NotInReadTransactionException;
}
