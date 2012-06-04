package jp.tonyu.soytext2.command;

import java.io.File;
import java.util.HashSet;

import jp.tonyu.db.DBAction;
import jp.tonyu.db.SqlJetRecord;
import jp.tonyu.db.SqlJetTableHelper;
import jp.tonyu.soytext2.document.IndexRecord;
import jp.tonyu.soytext2.document.SDB;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public class CheckDupIndex {
	public static void main(String[] args) throws SqlJetException {
		final SDB sdb=new SDB(new File(args[0]));
		final HashSet<String> chk=new HashSet<String>();
		sdb.readTransaction(new DBAction() {
			@Override
			public void run(SqlJetDb db) throws SqlJetException {
				IndexRecord ir = new IndexRecord();

				/*ISqlJetCursor cur2= sdb.indexTable().scope("document", "root@1.2010.tonyu.jp", "root@1.2010.tonyu.jp");
				while (!cur2.eof()) {
					ir.fetch(cur2);
					System.out.println(ir.id+" "+ir.name+" "+ir.value);
					cur2.delete();
					//cur2.next();
				}*/

				SqlJetTableHelper it = sdb.table(ir);
				ISqlJetCursor cur = it.order();
				while (!cur.eof()) {
					SqlJetRecord.fetch( ir , cur);
					cur.next();

					String nhd=(ir.document+"の"+ir.name+"は"+ir.value+"だ");
					if (chk.contains(nhd) && ir.name.equals("name")) {
						System.out.println("Dup - "+nhd);
					}
					chk.add(nhd);
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

