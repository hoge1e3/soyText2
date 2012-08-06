package jp.tonyu.soytext2.document;

import jp.tonyu.db.NotInWriteTransactionException;

public interface UpdatingIndexAction {

	boolean run(IndexRecord id) throws NotInWriteTransactionException;

}
