package jp.tonyu.db;

import java.sql.SQLException;


public class PrimaryKeySequence<T extends JDBCRecord> {
	int lastNumber;
	public static <T extends JDBCRecord> PrimaryKeySequence<T> create(JDBCTable<T> tbl) throws SQLException {
		return new PrimaryKeySequence<T>(tbl);
	}
	public PrimaryKeySequence(final JDBCTable<T> tbl) throws SQLException {
		super();
		JDBCHelper db = tbl.getDB();
		db.readTransaction(new DBAction() {

			@Override
			public void run(JDBCHelper db) throws SQLException {
			    OrderBy ord=new OrderBy().desc(tbl.primaryKeyName());
				JDBCRecordCursor<T> cur = tbl.order(ord);
				lastNumber=0;
				while (cur.next()) {
					JDBCRecord r = cur.fetch();
					Object v;
					try {
						v = r.getField(r.primaryKeyName()).get(r);
						if (v instanceof Integer) {
							Integer num = (Integer) v;
							if (num>lastNumber) lastNumber=num;
						}
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (NoSuchFieldException e) {
						e.printStackTrace();
					}
				}
			}
		},-1);
	}
	public int next() {
		lastNumber++;
		return lastNumber;
	}
	public int current() {
		return lastNumber;
	}
}
