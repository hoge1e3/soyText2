package jp.tonyu.soytext2.command;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jp.tonyu.db.DBAction;
import jp.tonyu.db.JDBCHelper;
import jp.tonyu.db.NotInReadTransactionException;
import jp.tonyu.db.NotInWriteTransactionException;
import jp.tonyu.db.WriteAction;
import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.document.IndexAction;
import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.document.SDB;

import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

// this is modified by brtest
public class Test {
    static String sel="select * from \"DocumentRecord\" where \"id\"=? order by \"id\";";
    static class DBAct extends WriteAction {

        @Override
        public void run(JDBCHelper db) throws SQLException, NotInWriteTransactionException {
            execQuery(db, "989@1.2010.tonyu.jp");
            execQuery(db, "55781@2.2010.tonyu.jp");
            execQuery(db, "974@1.2010.tonyu.jp");
            execQuery(db, "4470@1.2010.tonyu.jp");
            execQuery(db, "2695@1.2010.tonyu.jp");
            execQuery(db, "165@1.2010.tonyu.jp");
            execQuery(db, "4171@1.2010.tonyu.jp");

            /*db.execUpdate("delete from \"IndexRecord\" where \"document\">=? and \"document\"<=?;",
                    "57148@2.2010.tonyu.jp","57148@2.2010.tonyu.jp");*/
            //r.next();
            /*
            String insert="insert into \"IndexRecord\"(\"id\",\"document\",\"lastUpdate\",\"name\",\"value\") values (?,?,?,?,?);";
            db.execUpdate(insert,
                    307748,"57148@2.2010.tonyu.jp",57149,"#REFERS","989@1.2010.tonyu.jp");
            db.execUpdate(insert
                    ,307749,"57148@2.2010.tonyu.jp",57149,"#REFERS","55781@2.2010.tonyu.jp");
            //r.next();
            db.execUpdate(insert,
                    307750,"57148@2.2010.tonyu.jp",57149,"#INSTANCEOF","989@1.2010.tonyu.jp");
*/

            //db.execUpdate("insert into \"IndexRecord\"(\"id\",\"document\",\"lastUpdate\",\"name\",\"value\") values (?,?,?,?,?);",270005,"a",3,"b","c");
            //db.execUpdate("insert into \"IndexRecord\"(\"id\",\"document\",\"lastUpdate\",\"name\",\"value\") values (?,?,?,?,?);",270002,"b",3,"b","c");
            //db.execUpdate("insert into \"IndexRecord\"(\"id\",\"document\",\"lastUpdate\",\"name\",\"value\") values (?,?,?,?,?);",270003,"c",3,"b","c");
            //db.execUpdate("insert into \"IndexRecord\"(\"id\",\"document\",\"lastUpdate\",\"name\",\"value\") values (?,?,?,?,?);",270004,"d",3,"b","c");
        }

    }
    public static void main(String[] args) throws SQLException, ClassNotFoundException, NotInReadTransactionException {
        final SDB s=new SDB(new File("C:/Users/shinya/Dropbox/workspace/stStorage/db/1.2010.tonyu.jp/main.db"));
        JDBCHelper db=s.getHelper();
        ResultSet cur=db.execQuery("select * from DocumentRecord;");
        //cur.close();
        db.rollback();

    }
    public static void main234(String[] args) throws InterruptedException {
        final String s="abc";
        synchronized (s) {
            long l=System.currentTimeMillis();
            s.wait(500);
            long l2=System.currentTimeMillis();
            if (l2-l<500) {
                System.out.println("Notified");
            } else {
                System.out.println("Timed Out");
            }
        }
    }

    public static void main33(String[] args) throws InterruptedException {
        final String s="abc";
        Thread t=new Thread() {
            public void run() {
                synchronized (s) {
                    long l=System.currentTimeMillis();
                    try {
                        s.wait(500);
                    } catch (InterruptedException e) {
                        // TODO 自動生成された catch ブロック
                        e.printStackTrace();
                    }
                    long l2=System.currentTimeMillis();
                    System.out.println(l2-l);
                }
            }
        };
        t.start();

        Thread.sleep(1000);
        synchronized (s) {
            s.notify();
        }
        //t.interrupt();

    }
    public static void main2(String[] args) throws Exception {
        //SDB s=new SDB(new File("c:/bin/testcom.db"));
        final SDB s=new SDB(new File("C:/Users/shinya/Dropbox/workspace/stStorage/db/1.2010.tonyu.jp/main.db"));
        //
        JDBCHelper db=s.getHelper();
        /*final ResultSet r=db.execQuery("select * from \"DocumentRecord\" order by \"lastUpdate\" desc;");
        r.next();*/
        new Thread() {
            public void run() {
                try {
                    s.writeTransaction(new DBAct());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        new Thread() {
            public void run() {
                try {
                    s.writeTransaction(new DBAct());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        //s.close();

        /*Class.forName("org.sqlite.JDBC");
        Connection conn =
                    DriverManager.getConnection("jdbc:sqlite:c:/bin/comtest.db");
        conn.setAutoCommit(false);
        PreparedStatement st=conn.prepareStatement("insert into \"IndexRecord\"(\"id\",\"document\",\"lastUpdate\",\"name\",\"value\") values (270000,'a',4,'b','c')");
        st.executeUpdate();
        conn.commit();*/


    }
	protected static void execQuery(JDBCHelper db, String string) throws SQLException, NotInReadTransactionException {
	    ResultSet res=db.execQuery(sel, string);
	    Log.d("wait", string);
	    try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
	    res.close();
    }
    public static void main3(String[] args) {
		Context c;
		c=Context.enter();
		ScriptableObject root = c.initStandardObjects();
		Context.exit();

		c=Context.enter();
		c.setClassShutter(new ClassShutter() {

			@Override
			public boolean visibleToScripts(String fullClassName) {
				//if (fullClassName.indexOf("File")>=0) return false;
				//return true;
				return false;
			}
		});
		ScriptableObject.putProperty(root, "f", new File("tes.txt"));
		ScriptableObject.putProperty(root, "s", "ai,u");
		Object r = c.evaluateString(root, "s.split(/,/)[0]; f.getName();", "f", 1, null);
		System.out.println(r);
		Context.exit();
	}

}
