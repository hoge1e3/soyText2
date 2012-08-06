package jp.tonyu.soytext2.document;

import jp.tonyu.db.NotInReadTransactionException;

public interface DocumentAction {

    boolean run(DocumentRecord d) throws NotInReadTransactionException;
}
