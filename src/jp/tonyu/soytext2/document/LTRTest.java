package jp.tonyu.soytext2.document;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;

import jp.tonyu.db.JDBCHelper;
import jp.tonyu.db.NotInReadTransactionException;
import jp.tonyu.db.NotInWriteTransactionException;
import jp.tonyu.db.TransactionMode;
import jp.tonyu.db.WriteAction;
import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.document.LTRTest.DS;

public class LTRTest {
    static class DS implements DocumentSet {

        @Override
        public DocumentRecord newDocument() {
            // TODO 自動生成されたメソッド・スタブ
            return null;
        }

        @Override
        public DocumentRecord newDocument(String id) {
            // TODO 自動生成されたメソッド・スタブ
            return null;
        }

        @Override
        public void save(DocumentRecord d, PairSet<String, String> updatingIndex) {
            // TODO 自動生成されたメソッド・スタブ

        }

        @Override
        public void updateIndex(DocumentRecord d, PairSet<String, String> h) {
            // TODO 自動生成されたメソッド・スタブ

        }

        @Override
        public DocumentRecord byId(String id) {
            // TODO 自動生成されたメソッド・スタブ
            return null;
        }

        @Override
        public void all(DocumentAction a) {
            // TODO 自動生成されたメソッド・スタブ

        }
        TransactionMode mode=null;
        @Override
        public void transaction(TransactionMode mode) {
            assert this.mode==null;
            this.mode=mode;
            Log.d(this, "Entered "+mode);
        }

        @Override
        public void commit() {
            assert this.mode!=null;
            Log.d(this, "Commited "+mode);
            mode=null;
        }

        @Override
        public int log(String date, String action, String target, String option) {
            // TODO 自動生成されたメソッド・スタブ
            return 0;
        }

        @Override
        public String getDBID() {
            // TODO 自動生成されたメソッド・スタブ
            return null;
        }



        @Override
        public void searchByIndex(Map<String, String> keyValues, IndexAction a) {
            // TODO 自動生成されたメソッド・スタブ

        }

        @Override
        public boolean indexAvailable(String key) {
            // TODO 自動生成されたメソッド・スタブ
            return false;
        }

        @Override
        public File getBlob(String id) {
            // TODO 自動生成されたメソッド・スタブ
            return null;
        }

        @Override
        public void searchByIndex(Map<String, String> keyValues, UpdatingIndexAction a)
                throws NotInWriteTransactionException {
            // TODO 自動生成されたメソッド・スタブ

        }

        @Override
        public void all(UpdatingDocumentAction a) throws NotInWriteTransactionException {
            // TODO 自動生成されたメソッド・スタブ

        }

    }
    public static void main(String[] args) {
        DS d = new DS();
        final LooseTransaction ltr = new LooseTransaction(d);
        for (int i=0 ; i<10 ; i++) {
            final Runnable  wact= new Runnable() {
                int depth=0;
                Runnable t=this;
                @Override
                public void run() {
                    depth++;
                    if (depth>10) return;
                    try {
                        Thread.sleep((int)(Math.random()*50));
                    } catch (InterruptedException e) {
                        // TODO 自動生成された catch ブロック
                        e.printStackTrace();
                    }
                    int c=(int)(Math.random()*3)+1;
                    for (;c>0;c--) {
                        if (Math.random()<0.1) write(c);
                        else if (Math.random()<0.5) read(c);
                        else Log.d(this,this+"Nanika Sita");
                    }
                    depth--;
                }

                public void write(int c) {
                    Log.d(this, this+"["+depth+","+c+"]"+"Do write!");
                    ltr.write(new LooseWriteAction() {

                        @Override
                        public void run() throws NotInWriteTransactionException {
                            t.run();
                        }
                    });
                    Log.d(this, this+"["+depth+","+c+"]"+"Do write end!");
                }
                public void read(int c) {
                    Log.d(this, this+"["+depth+","+c+"]"+"Do read!");
                    ltr.read(new LooseReadAction() {

                        @Override
                        public void run() throws NotInReadTransactionException {
                            t.run();
                        }
                    });
                    Log.d(this, this+"["+depth+","+c+"]"+"Do read end!");
                }
            };
            /*d.transaction(TransactionMode.READ);
        d.commit();
        d.transaction(TransactionMode.WRITE);
        d.commit();*/
            new Thread(wact).start();
        }
    }
}
