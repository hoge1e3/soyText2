import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import jp.tonyu.util.Ref;
import jp.tonyu.util.Util;


public class Genereflect {
	public Ref<String> s;
	public Ref<Integer> i;
	
	public static void main(String[] args) throws SecurityException, NoSuchFieldException {
		ParameterizedType t = (ParameterizedType) Genereflect.class.getField("s").getGenericType();
		System.out.println(Util.join(",", t.getActualTypeArguments()));
//		System.out.println(t.getClass());
//		System.out.println(Util.join(",", t));
		/*System.out.println(Long.class.isAssignableFrom(Integer.class));
		System.out.println(Integer.class.isAssignableFrom(Long.class));
		Ref<Integer> i=Ref.create(3);
		Ref x=i;
		x.set("baka");
		System.out.println(x.get());
		int t=i.get();
		System.out.println(t);
		*/
		
	}
}
