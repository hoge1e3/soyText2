package jp.tonyu.db;

public class NotInReadTransactionException extends NotInWriteTransactionException {
    // W in R
    // !R in !W
}
