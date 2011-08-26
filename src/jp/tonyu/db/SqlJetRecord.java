package jp.tonyu.db;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;

import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.document.backup.Importer;
import jp.tonyu.util.Literal;

import org.tmatesoft.sqljet.core.table.ISqlJetCursor;

public class SqlJetRecord {
	public String getPrimaryKeyName() {
		return "id";
	}
	public String[] fieldOrder() {
		SortedSet<String> ress=new TreeSet<String>();
		for (Field f:getClass().getFields()) {
			String n=f.getName();
			if (getPrimaryKeyName().equals(n)) continue;
			ress.add(n);
		}
		String[] res=new String[ress.size()+1];
		int i=0;
		res[i]=getPrimaryKeyName();
		for (String s: ress) {
			i++;
			res[i]=s;
		}
		return res;
	}
	public void set(ISqlJetCursor cur) {
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
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	public void export(PrintStream p) {
		p.printf("[%s]\n", getClass().getName());
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
	public String importRecord(Scanner s) throws Exception {
		while (s.hasNextLine()) {
			String line=s.nextLine();
			Matcher m=Importer.table.matcher(line);
			String nextClassName = m.group(1);
			if (m.matches()) {
				return nextClassName;
			}
			m=Importer.field.matcher(line);
			if (m.matches()) {
				String key=m.group(1);
				String value=m.group(2);
				Field f = getField(key);
				if (String.class.isAssignableFrom(f.getType())) {
					f.set(this, Literal.fromLiteral(value));
				}
				if (Long.class.isAssignableFrom(f.getType())) {
					f.set(this, Long.parseLong(value));
				}
			}
		}
		return null;
	}
	public Field getField(String fname) throws NoSuchFieldException {
		return getClass().getField(fname);
	}
}
