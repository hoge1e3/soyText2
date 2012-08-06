package jp.tonyu.db;

public class TransactionContext {
    static ThreadLocal<TransactionMode> mode;
    public static void enter(TransactionMode m) {
        mode.set(m);
    }
    public static void ensureRead() throws NotInReadTransactionException {
        if (!TransactionMode.READ.equals(mode.get())) {
            throw new NotInReadTransactionException();
        }
    }
}
