package jp.tonyu.db;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
import org.tmatesoft.sqljet.core.internal.table.SqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;

/**
 * SqlJetRecord represents both "definition of a table" and "a record belonging the table".<BR>
 * 
 * To define fields, define public fields in subclass. 
 * The subclasses should contain the public field having the name equals to {@link primaryKeyName()} 
 * The field type should be int, long or String.<BR>
 * To define indexes, override {@link indexNames()}.
 * @author hoge1e3
 *
 */
public abstract class SqlJetRecord {
	/*private SqlJetHelper db;
	public SqlJetRecord(SqlJetHelper db) {
		this.db=db;
	}*/
	public SqlJetRecord newInstance() {
		return newInstance(getClass());
	}
	public <T extends SqlJetRecord> void copyTo(T dst) throws SqlJetException {
		try {
			for (String fname:columnOrder()) {
				Field f=getField(fname);
				f.set(dst, f.get(this));
			}
		} catch (NoSuchFieldException e) {
			throw new SqlJetException(e);
		} catch (IllegalArgumentException e) {
			throw new SqlJetException(e);
		} catch (IllegalAccessException e) {
			throw new SqlJetException(e);
		}
	}
	public <T extends SqlJetRecord> T dup(T thiz) throws SqlJetException {
		if (thiz!=this) Log.die("thiz must be equal to this");
		try {
			T res=(T)newInstance();
			for (String fname:columnOrder()) {
				Field f=getField(fname);
				f.set(res, f.get(this));
			}
			return res;
		} catch (NoSuchFieldException e) {
			throw new SqlJetException(e);
		} catch (IllegalArgumentException e) {
			throw new SqlJetException(e);
		} catch (IllegalAccessException e) {
			throw new SqlJetException(e);
		}
	}
	public <T extends SqlJetRecord> T newInstance(Class<T> src) {
		try {
			//Constructor<T> c = src.getConstructor(SqlJetHelper.class);
			return src.newInstance();//  c.newInstance(db);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
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
	/**
	 * Creates table according to the table definition of this record object.
	 * It will be called automatically by SqlJetHelper, users need not to call.
	 * @param tbl 
	 * @throws NoSuchFieldException
	 * @throws SqlJetException
	 */
	public void createTable(SqlJetTableHelper tbl) throws NoSuchFieldException, SqlJetException {
		for (String fname:columnOrder()) {
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
	/**
	 * Subclasses should override if indice are required.<BR>
	 * Each string may have two or more field names separated by comma.<BR>
	 * <BR>
	 * example: <code>return q("name","age","name,age");</code><BR>
	 * Note: These names are not the actual index names. They will be converted to
	 * the name like "Tablename_name1_name2..."
	 * @return array of index names
	 */
	public String[] indexNames() {
		return q();
	}
	/**
	 * Indicates the column order of the table. By default, the primary key are the first column and
	 * the rests are public fields sorted by names. 
	 * Subclass can override if the order should be changed. If there are fields that are not listed
	 * in this method, they will be not included to the column
	 * @return
	 */
	public String[] columnOrder() {
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
			String[] fo = columnOrder();
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
		for (String fname:columnOrder()) {
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
	public void export(PrintWriter p) {
		p.printf("[%s]\n", tableName());
		for (String fname:columnOrder()) {
			exportField(p, fname);
		}
	}
	public void exportField(PrintWriter p, String fname) {
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
