package jp.tonyu.soytext2.command;
import java.io.File;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import jp.tonyu.db.DBAction;
import jp.tonyu.db.SqlJetRecord;
import jp.tonyu.db.SqlJetTableHelper;
import jp.tonyu.soytext2.document.DocumentAction;
import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.document.IndexRecord;
import jp.tonyu.soytext2.document.SDB;


public class DumpIndex {
	public static void main(String[] args) throws SqlJetException {
		final SDB sdb=new SDB(new File(args[0]));
		sdb.readTransaction(new DBAction() {
			@Override
			public void run(SqlJetDb db) throws SqlJetException {
				IndexRecord ir = new IndexRecord();
				SqlJetTableHelper it = sdb.table(ir);
				ISqlJetCursor cur = it.order();
				while (!cur.eof()) {
					SqlJetRecord.fetch(ir, cur);
					cur.next();
					if (ir.name.equals("INDEX_CLASS")) {
						System.out.println(ir.document+"の"+ir.name+"は"+ir.value+"だ");
					}
				}
				cur.close();

				/*sdb.searchByIndex("INDEX_CLASS", "振り返り", new DocumentAction() {

					@Override
					public boolean run(DocumentRecord d) {
						System.out.println("IDXFND "+d);
						return false;
					}
				});*/
			}
		}, -1);
		sdb.close();
	}
}
