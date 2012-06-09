package jp.tonyu.soytext2.command;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jp.tonyu.db.DBAction;
import jp.tonyu.db.JDBCHelper;
import jp.tonyu.soytext2.document.IndexAction;
import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.document.SDB;

import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

// this is modified by brtest
public class Test {
    public static void main(String[] args) throws Exception {
        SDB s=new SDB(new File("c:/bin/testcom.db"));
        JDBCHelper db=s.getHelper();
        final ResultSet r=db.execQuery("select * from DocumentRecord;");
        r.next();
        s.writeTransaction(new DBAction() {
            @Override
            public void run(JDBCHelper db) throws SQLException {

                //[JDBC]Trans Start
                //[updateIndex]updateIndexIntrans (Document 57148@2.2010.tonyu.jp) with {#REFERS: 989@1.2010.tonyu.jp, #REFERS: 55781@2.2010.tonyu.jp, #INSTANCEOF: 989@1.2010.tonyu.jp, }
                //[query]delete from "IndexRecord" where "document">=? and "document"<=?;
                db.execUpdate("delete from IndexRecord where document>=? and document<=?;",
                        "57148@2.2010.tonyu.jp","57148@2.2010.tonyu.jp");
                r.next();
                //[ToValues]265370,57148@2.2010.tonyu.jp,57149,#REFERS,989@1.2010.tonyu.jp
                //[query]insert into "IndexRecord"("id","document","lastUpdate","name","value") values (?,?,?,?,?);
                db.execUpdate("insert into IndexRecord(id,document,lastUpdate,name,value) values (?,?,?,?,?);",
                        265370,"57148@2.2010.tonyu.jp",57149,"#REFERS","989@1.2010.tonyu.jp");
                //[ToValues]265371,57148@2.2010.tonyu.jp,57149,#REFERS,55781@2.2010.tonyu.jp
                //[query]insert into "IndexRecord"("id","document","lastUpdate","name","value") values (?,?,?,?,?);
                db.execUpdate("insert into IndexRecord(id,document,lastUpdate,name,value) values (?,?,?,?,?);"
                        ,265371,"57148@2.2010.tonyu.jp",57149,"#REFERS","55781@2.2010.tonyu.jp");
                //[ToValues]265372,57148@2.2010.tonyu.jp,57149,#INSTANCEOF,989@1.2010.tonyu.jp
                //[query]insert into "IndexRecord"("id","document","lastUpdate","name","value") values (?,?,?,?,?);
                r.next();
                db.execUpdate("insert into IndexRecord(id,document,lastUpdate,name,value) values (?,?,?,?,?);",
                        265372,"57148@2.2010.tonyu.jp",57149,"#INSTANCEOF","989@1.2010.tonyu.jp");


                //db.execUpdate("insert into \"IndexRecord\"(\"id\",\"document\",\"lastUpdate\",\"name\",\"value\") values (?,?,?,?,?);",270005,"a",3,"b","c");
                //db.execUpdate("insert into \"IndexRecord\"(\"id\",\"document\",\"lastUpdate\",\"name\",\"value\") values (?,?,?,?,?);",270002,"b",3,"b","c");
                //db.execUpdate("insert into \"IndexRecord\"(\"id\",\"document\",\"lastUpdate\",\"name\",\"value\") values (?,?,?,?,?);",270003,"c",3,"b","c");
                //db.execUpdate("insert into \"IndexRecord\"(\"id\",\"document\",\"lastUpdate\",\"name\",\"value\") values (?,?,?,?,?);",270004,"d",3,"b","c");
            }
        }, -1);
        /*Class.forName("org.sqlite.JDBC");
        Connection conn =
                    DriverManager.getConnection("jdbc:sqlite:c:/bin/comtest.db");
        conn.setAutoCommit(false);
        PreparedStatement st=conn.prepareStatement("insert into \"IndexRecord\"(\"id\",\"document\",\"lastUpdate\",\"name\",\"value\") values (270000,'a',4,'b','c')");
        st.executeUpdate();
        conn.commit();*/

        s.close();

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
