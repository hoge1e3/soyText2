package jp.tonyu.db;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.document.backup.Importer;
import jp.tonyu.util.Literal;
import jp.tonyu.util.Util;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;

public abstract class SqlJetRecord {
	public void createTableAndIndex(SqlJetHelper db) throws SqlJetException, NoSuchFieldException {
		SqlJetTableHelper tbl=db.table(this);
		createTable(tbl);
		createIndex(tbl);
	}
	public void createIndex(SqlJetTableHelper tbl) throws SqlJetException {
		for (String iname:indexNames()) {
			tbl.createIndex(iname);
		}
	}
	public abstract String tableName();/* {
		return getClass().getName().replaceAll("\\.", "_");
	}*/
	public void createTable(SqlJetTableHelper tbl) throws NoSuchFieldException, SqlJetException {
		for (String fname:fieldOrder()) {
			Field f=getField(fname);
			String type="TEXT";
			Class ftype=f.getType();
			if (ftype.equals(Long.TYPE) || ftype.equals(Integer.TYPE)) {
				type="INTEGER";
			}
			if (fname.equals(primaryKeyName())) {
				type+=" PRIMARY KEY";
			}
			tbl=tbl.a(fname, type);
		}
		tbl.create();
	}
	public String primaryKeyName() {
		return "id";
	}

	public String[] indexNames() {
		return q();
	}
	public String[] fieldOrder() {
		SortedSet<String> ress=new TreeSet<String>();
		for (Field f:getClass().getFields()) {
			String n=f.getName();
			if (primaryKeyName().equals(n)) continue;
			ress.add(n);
		}
		String[] res=new String[ress.size()+1];
		int i=0;
		res[i]=primaryKeyName();
		for (String s: ress) {
			i++;
			res[i]=s;
		}
		return res;
	}
	public Object[] toValues() throws SqlJetException {
		try {
			String[] fo = fieldOrder();
			Object[] values=new Object[fo.length];
			int i=0;
			for (String fn:fo) {
				Field f = getField(fn);
				values[i]=f.get(this);
				i++;
			}
			Log.d("ToValues", Util.join(",",values));
			return (values);
		} catch (NoSuchFieldException e) {
			throw new SqlJetException(e);
		} catch (IllegalArgumentException e) {
			throw new SqlJetException(e);
		} catch (IllegalAccessException e) {
			throw new SqlJetException(e);
		}
	}
	public void insertTo(ISqlJetTable t) throws SqlJetException {
		t.insert(toValues());
	}
	public void fetch(ISqlJetCursor cur) throws SqlJetException {
		int i=0;
		for (String fname:fieldOrder()) {
			try {
				Field f = getField(fname);
				Class t=f.getType();
				if (t.equals(Long.TYPE)) {
					long r=cur.getInteger(i);
					f.set(this, r);
				} else if (t.equals(Integer.TYPE)) {
					int r=(int)cur.getInteger(i);
					f.set(this, r);
				} else if (String.class.isAssignableFrom(t)) {
					String r=cur.getString(i);
					f.set(this, r);
				} else {
					Log.die(t+" cannot be assigned");
				}
				i++;
			} catch (NoSuchFieldException e) {
				throw new SqlJetException(e);
			} catch (IllegalArgumentException e) {
				throw new SqlJetException(e);
			} catch (IllegalAccessException e) {
				throw new SqlJetException(e);
			}
			
		}
	}
	public void update(ISqlJetCursor cur) throws SqlJetException {
		cur.update(toValues());
	}
	public void export(PrintStream p) {
		p.printf("[%s]\n", tableName());
		for (String fname:fieldOrder()) {
			exportField(p, fname);
		}
	}
	public void exportField(PrintStream p, String fname) {
		p.printf("%s=",fname);
		try {
			Field f=getField(fname);
			Object v=f.get(this);
			if (v instanceof String) {
				String str = (String) v;
				p.println(Literal.toLiteral(str));
			} else {
				p.println(v);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static String firstTableName(Scanner s) {
		while (s.hasNextLine()) {
			String line=s.nextLine();
			Matcher m=Importer.table.matcher(line);
			if (m.matches()) {
				String nextClassName = m.group(1);
				return nextClassName;
			}
		}
		return null;
	}
	private static final Pattern table=Pattern.compile("\\[([\\d\\w]+)\\]");
	private static final Pattern field=Pattern.compile("([\\d\\w]+)=(.*)");
	public String importRecord(Scanner s) throws SqlJetException {
		try {
			while (s.hasNextLine()) {
				String line=s.nextLine();
				Matcher m=table.matcher(line);
				if (m.matches()) {
					String nextClassName = m.group(1);
					return nextClassName;
				}
				m=field.matcher(line);
				if (m.matches()) {
					String key=m.group(1);
					String value=m.group(2);
					Field f = getField(key);
					Class<?> ftype = f.getType();
					if (String.class.isAssignableFrom(ftype)) {
						f.set(this, Literal.fromLiteral(value));
					}
					if (Integer.TYPE.equals(ftype)) {
						f.set(this, Integer.parseInt(value));
					}
					if (Long.TYPE.equals(ftype) ) {
						f.set(this, Long.parseLong(value));
					}
				} else {
					Log.d(this, "Warning : no match line:"+line);
				}
			}
			return null;
		}catch(NoSuchFieldException e) {
			throw new SqlJetException(e);
		} catch (IllegalArgumentException e) {
			throw new SqlJetException(e);
		} catch (IllegalAccessException e) {
			throw new SqlJetException(e);
		}
	}
	public Field getField(String fname) throws NoSuchFieldException {
		return getClass().getField(fname);
	}
	public static <T> T[] q(T... ts) {
		return ts;
	}
	public void insertTo(SqlJetTableHelper curTbl) throws SqlJetException {
		insertTo(curTbl.table());
	}
}
