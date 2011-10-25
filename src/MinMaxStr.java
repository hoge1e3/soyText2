import jp.tonyu.soytext2.document.SDB;


public class MinMaxStr {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String t=new String(new char[]{32767,32767,32767});
		String t2=new String(new char[]{0});
		System.out.println(SDB.MIN_STRING.compareTo(t2)); // MIN_STR<t2
		System.out.println(SDB.MAX_STRING.compareTo(t));  // MAX_STR>t
		System.out.println(SDB.MAX_STRING);
		System.out.println(SDB.MAX_STRING.length());

	}

}
