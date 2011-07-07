import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.js.JSSession;
import jp.tonyu.util.Maps;


public class ReflectMethod implements Wrappable {
	/*public void foo24(int x,int y,int z,int t){
		System.out.println("foo24-4");
	}*/
	public void foo24(int x,String y){
		System.out.println("foo24is-2:"+(x+y));
	
	}
	public void foo24(String x,int y){
		System.out.println("foo24si-2:"+(x+y));
	
	}
	public static void main(String[] args) {
		JSSession s = new JSSession();
		
		Object r = s.eval("test","a.foo24(3,4);", Maps.create("a",(Object)new ReflectMethod()) );
		System.out.println(r);

	}
}
