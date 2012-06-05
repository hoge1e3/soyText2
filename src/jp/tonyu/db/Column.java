package jp.tonyu.db;

public class Column {
	String name;
	String type;
	public Column(String name, String type) {
		super();
		this.name = name;
		this.type = type;
	}
	@Override
	public String toString() {
		return JDBCTable.symbol(name)+" "+type;
	}
}
