package jp.tonyu.db;

public class OrderByElem {
    String name;
    boolean isDescend;
    public OrderByElem(String name, boolean isDescend) {
        super();
        this.name=name;
        this.isDescend=isDescend;
    }
    public static OrderByElem parse(String spec) {
        if (spec.startsWith("-")) {
            return new OrderByElem(spec.substring(1), true);
        }
        return new OrderByElem(spec, false);
    }
    @Override
    public String toString() {
        return JDBCTable.symbol(name)+(isDescend?" desc":"");
    }
}
