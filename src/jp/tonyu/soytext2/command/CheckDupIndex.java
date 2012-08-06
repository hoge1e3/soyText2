package jp.tonyu.soytext2.command;

import java.io.File;
import java.sql.SQLException;
import java.util.HashSet;

import jp.tonyu.db.DBAction;
import jp.tonyu.db.JDBCHelper;
import jp.tonyu.db.JDBCRecordCursor;
import jp.tonyu.db.NotInReadTransactionException;
import jp.tonyu.db.ReadAction;
import jp.tonyu.soytext2.document.IndexRecord;
import jp.tonyu.soytext2.document.SDB;

public class CheckDupIndex {
	public static void main(String[] args) throws SQLException, ClassNotFoundException, NotInReadTransactionException {
		final SDB sdb=new SDB(new File(args[0]));
		final HashSet<String> chk=new HashSet<String>();
		sdb.readTransaction(new ReadAction() {
			@Override
			public void run(JDBCHelper db) throws SQLException, NotInReadTransactionException {

				JDBCRecordCursor<IndexRecord> cur=sdb.indexTable().order();
				while (cur.next()) {
					IndexRecord ir=cur.fetch();

					String nhd=(ir.document+"の"+ir.name+"は"+ir.value+"だ");
					if (chk.contains(nhd) && ir.name.equals("name")) {
						System.out.println("Dup - "+nhd);
					}
					chk.add(nhd);
				}
				cur.close();

			}
		});
		sdb.close();
	}
}

