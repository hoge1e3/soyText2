package jp.tonyu.soytext2.document;

import jp.tonyu.db.NotInReadTransactionException;

public interface IndexAction {

	boolean run(IndexRecord id) throws NotInReadTransactionException;

}
