package jp.tonyu.soytext2.webapi.youtube;

import java.util.HashMap;
import java.util.Map;

import jp.tonyu.js.Scriptables;
import jp.tonyu.js.StringPropAction;
import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.js.JSSession;
import jp.tonyu.soytext2.js.MapScriptable;
import jp.tonyu.soytext2.webapi.ItemIterator;
import jp.tonyu.soytext2.webapi.WebAPI;
import jp.tonyu.soytext2.webapi.WebAPISearcher;
import jp.tonyu.soytext2.webapi.WebClient;

import org.eclipse.jetty.xml.XmlParser.Node;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;


public class YoutubeAPI implements WebAPI,Wrappable {

	@Override
	public void search(Map<String, String> params, ItemIterator iter)
			throws Exception {
		
		WebClient w= new WebClient("http://gdata.youtube.com/feeds/api/videos", params);
		Node n=w.execXML();
		for (Object o:n) {
			if (o instanceof Node) {
				Node entry = (Node) o;
				if ("entry".equals(entry.getTag())) {
					HashMap<String, String>item=new HashMap<String, String>();
					try {
						getAttr(entry, "id", item, "id");
						item.put("id",item.get("id").replaceAll("^.*/", ""));
						item.put("動画番号", item.get("id"));
					} catch(Exception e){}
					
					getAttr(entry, "title", item, "タイトル");
					getAttr(entry, "content", item, "説明文");
					getAttr(entry.get("author"), "name", item, "作者");

					Node group=entry.get("group");
					try {
						item.put("サムネイル", group.get("thumbnail").getAttribute("url"));
						getAttr(group, "duration", item, "再生時間");
					} catch(Exception e){}
					try {
						StringBuilder bl=new StringBuilder();
						for (Object cs: entry) {
							if (cs instanceof Node) {
								Node c = (Node) cs;
								if ("category".equals(c.getTag())) {
									if (bl.length()>0) bl.append(",");
									String term = c.getAttribute("term");
									if (term!=null && !term.contains("http://"))  bl.append(term);									
								}
							}
						}
						item.put("カテゴリー", bl.toString());
					} catch(Exception e){}
					boolean brk=iter.iterate(item);
					if (brk) break;
					if (debug) {
						System.out.println("---------------------------------------");
						for (Object a:entry) {
							System.out.println(a);
						}
						System.out.println("-----------grp-----------------------");
						for (Object a:entry.get("group")) {
							System.out.println(a);
						}
						
					}
				}
			}
		}
	}
	public static void getAttr(Node src, String srcName, Map<String,String> dst ,String dstName) {
		try {
			dst.put(dstName, src.get(srcName).get(0).toString().trim());
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	public static <K,V> void putIfEmpty(Map<K, V> keyMap, K key, V value)  {
		if (keyMap.containsKey(key)) return;
		keyMap.put(key, value);		
	}
	/*
	public void search(Scriptable params, final Function iter) throws Exception {
		final JSSession s=JSSession.cur.get();
		final HashMap<String, String> sparams = new HashMap<String, String>();
		Scriptables.each(params, new StringPropAction() {
			@Override
			public void run(String key, Object value) {
				sparams.put(key,value+"");
			}
		});
		search(sparams,new ItemIterator() {
			@Override
			public void iterate(Map<String, String> item) {
				MapScriptable i = new MapScriptable(item);
				s.call(iter, new Object[]{i});
			}
		});
	}*/
	public WebAPISearcher searcher() {
		return new WebAPISearcher(this);
	}
	static boolean debug=false;
	public static void main(String[] args) throws Exception {
		HashMap<String, String>params=new HashMap<String, String>();
		//debug=true;
		params.put("vq", "Super");
		new YoutubeAPI().search(params, new ItemIterator() {
			
			@Override
			public boolean iterate(Map<String, String> item) {
				System.out.println(item);
				return false;
			}
		});
	}
}
