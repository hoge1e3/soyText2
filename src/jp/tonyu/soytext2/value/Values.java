package jp.tonyu.soytext2.value;

public class Values {
	static final String LENGTH="length";
	public static Object[] toArrayArgs(Value v) {
		try {
			int len=Integer.parseInt(""+v.get(LENGTH, v));
			Object[] res=new Object[len];
			for (int i=0; i<len ;i++) {
				res[i]=v.get(i+"", v);
			}
			return res;
		} catch (Exception e) {
			return new Object[0];
		}
	}
}
