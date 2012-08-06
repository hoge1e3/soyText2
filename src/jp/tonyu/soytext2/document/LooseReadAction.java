package jp.tonyu.soytext2.document;

import jp.tonyu.db.NotInReadTransactionException;

public interface LooseReadAction {
    public void run() throws NotInReadTransactionException;
}
