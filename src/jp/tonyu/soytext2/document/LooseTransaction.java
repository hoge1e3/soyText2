package jp.tonyu.soytext2.document;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import jp.tonyu.db.NotInReadTransactionException;
import jp.tonyu.db.NotInWriteTransactionException;
import jp.tonyu.db.TransactionMode;
import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.document.LooseTransaction.Action;
import jp.tonyu.soytext2.js.Debug;
import jp.tonyu.db.ReadAction;

public class LooseTransaction {
    enum Action {enter,nop,/*lazy,*/join};
    DocumentSet documentSet;
    public LooseTransaction(DocumentSet documentSet) {
        super();
        this.documentSet = documentSet;
    }
    TransactionMode mode=null;
    Set<Thread> entering=new HashSet<Thread>();
    public synchronized TransactionMode otherThreadMode() {
        Thread t=Thread.currentThread();
        if (entering.isEmpty()) return null;
        if (entering.size()==1 && entering.contains(t)) return null;
        return mode;
    }
    public synchronized TransactionMode thisThreadMode() {
        Thread t=Thread.currentThread();
        if (!entering.contains(t)) return null;
        return mode;
    }
    public void read(LooseReadAction r) {
        Action a = enterRead();
        Log.d(this, "Read Trans action =" +a);

        try {
            assert entering.contains(Thread.currentThread());
            r.run();
        } catch (NotInReadTransactionException e) {
            e.printStackTrace();
        } finally {
            if (a!=Action.nop )commit();
        }
    }
    /*
     * other  this  do
     * n      n     1.enter
     * n      r     2.nop
     * n      w     3.nop
     * r      n     4.join
     * r      r     5.nop
     * r      w     N/A
     * w      n     6.wait
     * w      r     N/A
     * w      w     N/A
     */
    private synchronized Action enterRead()  {
        Thread th=Thread.currentThread();
        while(true) {
            Object o=otherThreadMode();
            Object t=thisThreadMode();
            if (o==null && t==null) {
                assert entering.isEmpty();
                assert mode==null;
                // enter 1.
                documentSet.transaction(TransactionMode.READ);
                entering.add(th);
                Log.d(this, th+" added for read 1.");
                mode=TransactionMode.READ;
                return Action.enter;
            } else if (t!=null) {
                assert entering.contains(th);
                assert
                (o==null && TransactionMode.READ.equals(t)) || // 2.
                (o==null && TransactionMode.WRITE.equals(t)) || // 3.
                (TransactionMode.READ.equals(o) && TransactionMode.READ.equals(t))    // 5.
                ;
                // do nothing  2.  3.  5.
                return Action.nop;
            } else {  //  o!=null   t==null
                assert o!=null;
                assert t==null;
                assert !entering.contains(th);
                if (TransactionMode.READ.equals(mode)) {
                    assert (TransactionMode.READ.equals(o) && t==null);
                    // join  4.
                    assert !entering.contains(th);
                    entering.add(th);
                    Log.d(this, th+" added for join read ");
                    return Action.join;
                } else if (TransactionMode.WRITE.equals(mode)) {
                    assert (TransactionMode.WRITE.equals(o) && t==null);
                    // 6. wait
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    assert false;
                }
            }
        }
    }
    /*
     * other  this  do
     * n      n     1.enter
     * n      r     2.lazy
     * n      w     3.nop
     * r      n     4.wait
     * r      r     5.lazy
     * r      w     N/A
     * w      n     6.wait
     * w      r     N/A
     * w      w     N/A
     */
    private synchronized Action enterWrite() {
        Thread th=Thread.currentThread();
        while(true) {
            TransactionMode o=otherThreadMode();
            TransactionMode t=thisThreadMode();
            if (o==null && t==null) {
                assert entering.isEmpty();
                assert mode==null;
                // enter 1.
                documentSet.transaction(TransactionMode.WRITE);
                entering.add(th);
                Log.d(this, th+" added for write 1.");
                mode=TransactionMode.READ;
                return Action.enter;
            } else if (TransactionMode.READ.equals(t)) {
                assert entering.contains(th);
                assert
                (o==null && TransactionMode.READ.equals(t)) || // 2.
                (TransactionMode.READ.equals(o) && TransactionMode.READ.equals(t));    // 5.

                // lazy  2.  5.
                //return Action.lazy;
                throw new RuntimeException("Already in read transaction");
            } else if (TransactionMode.WRITE.equals(t)) {
                assert entering.contains(th);
                assert (o==null && TransactionMode.WRITE.equals(t)); // 3.
                // nop  3.
                return Action.nop;
            } else { // o!=null &&  t==null
                assert o!=null;
                assert t==null;
                assert !entering.contains(th);
                // 4. 6. wait

                try {
                    wait();
                } catch (InterruptedException e) {
                    // TODO 自動生成された catch ブロック
                    e.printStackTrace();
                }
            }

        }
    }
    public void write(LooseWriteAction r) {
        Action a= enterWrite();
        Log.d(this, "Write Trans action =" +a);
        //if (a!=Action.lazy) {
            assert entering.contains(Thread.currentThread());
            try {
                r.run();
            } catch (NotInWriteTransactionException e) {
                e.printStackTrace();
            } finally {
                if (a!=Action.nop) commit();
            }
        /*} else {
            reserveWrite(r);
        }*/
    }
    /*List<LooseWriteAction> reserved= new Vector<LooseWriteAction>();
    private void reserveWrite(LooseWriteAction r) {
        Log.d(this,"Reserved");
        synchronized (reserved) {
            reserved.add(r);
        }
    }*/
    private void commit() {
        synchronized (this) {
            Thread th=Thread.currentThread();
            assert entering.contains(th);
            entering.remove(th);
            if (entering.isEmpty()) {
                documentSet.commit();
                mode=null;
            }
            if (mode==null) {
                /*List<LooseWriteAction> old;
                synchronized (reserved) {
                    old=reserved;
                    reserved=new Vector<LooseWriteAction>();
                }
                for (LooseWriteAction r:old) {
                    write(r);
                }*/
            }
            notifyAll();
        }
    }
}
