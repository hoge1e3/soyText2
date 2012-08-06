package jp.tonyu.soytext2.document;

import jp.tonyu.db.NotInWriteTransactionException;

public interface UpdatingDocumentAction {

    boolean run(DocumentRecord d) throws NotInWriteTransactionException;
}
