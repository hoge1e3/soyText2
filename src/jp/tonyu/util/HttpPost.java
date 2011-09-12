package jp.tonyu.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;
import jp.tonyu.soytext2.servlet.Html;

public class HttpPost {
	public static String send(String urls, Map<String,String> params ) throws IOException{
		final StringBuilder data=new StringBuilder();
		StringBuilder res=new StringBuilder();
		Maps.entries(params).each(new MapAction<String, String>() {
			@Override
			public void run(String key, String value) {
				data.append(Html.p("%u=%u&", key,value));
			}
		});
		URL url = new URL(urls);
		HttpURLConnection htpcon = (HttpURLConnection)url.openConnection();
		htpcon.setRequestMethod("POST");
		htpcon.setDoOutput(true);
		htpcon.connect();
		OutputStreamWriter out = new OutputStreamWriter( htpcon.getOutputStream() );
		out.write(data+"\n");
		out.flush();
		out.close();
		Scanner s=new Scanner(new InputStreamReader(htpcon.getInputStream() ,"utf-8"));
		while (s.hasNextLine()) {
			res.append(s.nextLine()+"\n");
		}
		s.close();
		return res+"";
	}
	public static void main(String[] args) throws IOException {
		
		String res=send("http://localhost:8080/soytext2/exec/1269@1.2010.tonyu.jp",
				Maps.create("data", "てすと").p("de_ta", "とすて"));
		System.out.println(res);
	}
}