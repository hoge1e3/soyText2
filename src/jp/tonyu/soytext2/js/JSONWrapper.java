package jp.tonyu.soytext2.js;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import net.arnx.jsonic.JSON;
import jp.tonyu.js.BlankScriptableObject;
import jp.tonyu.js.Scriptables;
import jp.tonyu.js.Wrappable;
import jp.tonyu.util.MapAction;
import jp.tonyu.util.Maps;

public class JSONWrapper implements Wrappable{
	JSON json=new JSON() {
		protected Object preformat(Context context, Object value) throws Exception {
			//System.out.println("preform "+value);
			if (value instanceof NativeArray) {
				NativeArray ar = (NativeArray) value;
				return Scriptables.toArray(ar);
			} else if (value instanceof Scriptable) {
				Scriptable s = (Scriptable) value;
				return Scriptables.toStringKeyMap(s);
			}
			return super.preformat(context, value);
		}
		
	};
	public void setPrettyPrint(boolean p) {
		json.setPrettyPrint(p);
	}
	protected Object postparse(Object value, final JSSession jssession) {
		if (value instanceof Map) {
			Map<Object,Object> m=(Map<Object,Object>)value;
			//final BlankScriptableObject res = new BlankScriptableObject();
			final Scriptable res=jssession.newObject();
			Maps.entries(m).each(new MapAction<Object, Object>() {
				@Override
				public void run(Object key, Object value) {
					ScriptableObject.putProperty(res, key+"", postparse(value,jssession));
				}
			});
			return res;
		}
		if (value instanceof List<?>) {
			//Vector<Object> rv=new Vector<Object>();
			Scriptable a = jssession.newArray();
			int i=0;
			for (Object o:(List<?>)value) {
				ScriptableObject.putProperty(a, i, postparse(o,jssession));
				i++;
			}
			return a;
		}
		return value;
	}
	/*private Scriptable newObject() {
		return (Scriptable)objFactory.construct(org.mozilla.javascript.Context.getCurrentContext(), null, new Object[0]);
	}
	private Scriptable newArray() {
		return (Scriptable)aryFactory.construct(org.mozilla.javascript.Context.getCurrentContext(), null, new Object[0]);
	}*/
	public String format(Object s) {
		return 	json.format(s);
	}
	public Object parse(String source) {
		Object r=json.parse(source);
		return postparse(r,JSSession.cur.get());
	}	/*Function objFactory,aryFactory;
	public void setFactories(Function object,Function array) {
		objFactory=object;
		aryFactory=array;
	}*/
	public static void main(String[] args) {
		JSSession.cur.enter(new JSSession(), new Runnable() {
			
			@Override
			public void run() {
				JSONWrapper json=new JSONWrapper();
				JSSession j=JSSession.cur.get();
				String src=
						"var res='';\n" +
						"var r={a:3, b:[2,4,'a']}; \n" +
						"var sep='\\n';\n" +
						"res+=(r.b)+sep;\n" +
						"json.setPrettyPrint(true);\n"+
						"var form=json.format(r);\n" +
						"res+=form+sep;\n" +
						"var r2=json.parse(form);\n" +
						"res+=(r2)+sep;\n" +
						"res+=(r2.a)+sep;\n" +
						"res+=(r2.b)+sep;\n" +
						"res+=(r2.b[1])+sep;\n" +
						"res;";
				Object r=j.eval("json", src, Maps.create("json", (Object)json));
				System.out.println(r);
				
			}
		});
	}
}
