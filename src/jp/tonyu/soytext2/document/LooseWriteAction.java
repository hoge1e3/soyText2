package jp.tonyu.soytext2.document;

import jp.tonyu.db.NotInWriteTransactionException;

public interface LooseWriteAction {
    public void run() throws NotInWriteTransactionException;
}
