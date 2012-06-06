package jp.tonyu.soytext2.command;
import java.io.File;
import java.sql.SQLException;

import jp.tonyu.db.DBAction;
import jp.tonyu.db.JDBCHelper;
import jp.tonyu.db.JDBCRecordCursor;
import jp.tonyu.soytext2.document.IndexRecord;
import jp.tonyu.soytext2.document.SDB;


public class DumpIndex {
	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		final SDB sdb=new SDB(new File(args[0]));
		sdb.readTransaction(new DBAction() {
			@Override
			public void run(JDBCHelper db) throws SQLException {
				IndexRecord ir = new IndexRecord();
				JDBCRecordCursor<IndexRecord> cur=sdb.indexTable().all();
				while (cur.next()) {
					ir=cur.fetch();
				    if (ir.name.equals("INDEX_CLASS")) {
						System.out.println(ir.document+"の"+ir.name+"は"+ir.value+"だ");
					}
				}
				cur.close();
			}
		}, -1);
		sdb.close();
	}
}
