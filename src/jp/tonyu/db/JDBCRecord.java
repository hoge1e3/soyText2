package jp.tonyu.db;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import jp.tonyu.debug.Log;
import jp.tonyu.util.Literal;
import jp.tonyu.util.MapAction;
import jp.tonyu.util.Maps;
import jp.tonyu.util.Util;


/**
 * SqlJetRecord represents both "definition of a table" and "a record belonging the table".<BR>
 *
 * To define columns, define public fields in subclass.
 * The subclasses should contain the public field having the name equals to {@link primaryKeyName()}
 * The field type should be int, long or String.<BR>
 * To define indexes, override {@link indexNames()}.
 * @author hoge1e3
 *
 */
public abstract class JDBCRecord implements Cloneable {


	/*public boolean exists(JDBCHelper h) {
		return h.table(this).exists();
	}*/
	public  <T extends JDBCRecord> void copyTo(T dst)  {

		try {
			for (String fname:columnOrder()) {
				Field f=getField(fname);
				f.set(dst, f.get(this));
			}
		} catch (NoSuchFieldException e) {
			Log.die(e);
		} catch (IllegalArgumentException e) {
			Log.die(e);
		} catch (IllegalAccessException e) {
			Log.die(e);
		}
	}
	public <T extends JDBCRecord> T dup(T thiz)  {
		if (thiz!=this) Log.die("thiz must be equal to this");
		try {
			return (T)clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static <T extends JDBCRecord> T newInstance(Class<T> src) {
		try {
			return src.newInstance();
		} catch (SecurityException e) {
			Log.die(e);
		} catch (IllegalArgumentException e) {
			Log.die(e);
		} catch (InstantiationException e) {
			Log.die(e);
		} catch (IllegalAccessException e) {
			Log.die(e);
		}
		return null;

	}
	public abstract String tableName();/* {
	return getClass().getName().replaceAll("\\.", "_");
}*/
	/*
	 * Creates table according to the table definition of this record object.
	 * It will be called automatically by JDBCHelper, users need not to call.
	 * @param tbl
	 * @throws NoSuchFieldException
	 * @throws SQLException
	 */
	/*public static void createTable(JDBCRecord r,JDBCTable tbl) throws SQLException {
		tbl.create();
	}*/
	public String primaryKeyName() {
		return "id";
	}
	/**
	 * Subclasses should override if index are required.<BR>
	 * The return values is a string array. Each string represents "index spec".
	 * Index spec consists of field name(s) separated by comma.<BR>
	 * If the name begins with '-', it indicates descendant order
	 * <BR>
	 * example: <code>return q("name","age","name,age","name,-occupation");</code><BR>
	 * Note: These names are not the actual index names. They will be converted to
	 * the name like "Tablename_name1_name2..."
	 * @return array of index specs
	 */
	public String[] indexSpecs() {
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
			if (Modifier.isStatic(f.getModifiers())) continue;
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
	public Object[] toValues() {
		return toValues(false);
	}
	public Object[] toValues(boolean noPrimaryKeyValue) {
		try {
			String[] fo = columnOrder();
			Object[] values=new Object[fo.length-(noPrimaryKeyValue?1:0)];
			int i=0;
			for (String fn:fo) {
				Field f = getField(fn);
				if (noPrimaryKeyValue && fn.equals(primaryKeyName())) continue;
				values[i]=f.get(this);
				i++;
			}
			Log.d("ToValues", Util.join(",",values));
			return (values);
		} catch (NoSuchFieldException e) {
			Log.die(e);
		} catch (IllegalArgumentException e) {
			Log.die(e);
		} catch (IllegalAccessException e) {
			Log.die(e);
		}
		return null;
	}
	/*
	 * inserts this record into t
	 * @param t  A table to which be inserted. get with DBHelper::table(this)
	 * @throws SQLException
	 *
	public static void insertTo(JDBCRecord r, JDBCTable t) throws SQLException {
		t.insert(r.toValues());
	}*/
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
	/*public static String firstTableName(Scanner s) {
		while (s.hasNextLine()) {
			String line=s.nextLine();
			Matcher m=Importer.table.matcher(line);
			if (m.matches()) {
				String nextClassName = m.group(1);
				return nextClassName;
			}
		}
		return null;
	}*/
	/*private static final Pattern table=Pattern.compile("\\[([\\d\\w]+)\\]");
	private static final Pattern field=Pattern.compile("([\\d\\w]+)=(.*)");
	public String importRecord(Scanner s) throws SQLException {
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
			throw new SQLException(e);
		} catch (IllegalArgumentException e) {
			throw new SQLException(e);
		} catch (IllegalAccessException e) {
			throw new SQLException(e);
		}
	}*/
	public Field getField(String fname) throws NoSuchFieldException {
		return getClass().getField(fname);
	}
	public static <T> T[] q(T... ts) {
		return ts;
	}
	/*public void insertTo(JDBCTable curTbl) throws SQLException {
		insertTo(this, curTbl);
	}*/
	public Map<String,Object> toMap() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		HashMap<String, Object> res = new HashMap<String, Object>();
		for (String fn:columnOrder()) {
			Field f=getField(fn);
			Object value=f.get(this);
			res.put(fn, value);
		}
		return res;
	}
	public void copyFrom(Map<String, Object> m) {
		Maps.entries(m).each(new MapAction<String, Object>() {

			@Override
			public void run(String key, Object value) {
				try {
					Field f=getField(key);
					if (value!=null) {
						Class<? extends Object> vclass = value.getClass();
						Class<?> fclass = f.getType();
						if (!fclass.isAssignableFrom(vclass)) {
							if (value instanceof BigDecimal) {
								BigDecimal d = (BigDecimal) value;
								//Log.d("cast",fclass);
								if (fclass.isAssignableFrom(Integer.TYPE)) {
									value=d.intValue();
								}
								if (fclass.isAssignableFrom(Long.TYPE)) {
									value=d.longValue();
								}
								if (fclass.isAssignableFrom(Double.TYPE)) {
									value=d.doubleValue();
								}
							}
						}
					}
					f.set(JDBCRecord.this, value);
				} catch (NoSuchFieldException e) {
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		});

	}
}
