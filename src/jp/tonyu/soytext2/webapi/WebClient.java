package jp.tonyu.soytext2.webapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jetty.xml.XmlParser;
import org.eclipse.jetty.xml.XmlParser.Node;
import org.xml.sax.SAXException;

public class WebClient {
	String url;
	Map<String,String> params=new Hashtable<String, String>();
	public Node execXML() throws MalformedURLException, IOException {
		StringBuilder b=new StringBuilder(url);
		String sep="?";
		for (String k:params.keySet()) {
			b.append(sep);sep="&";
			try {
				b.append( URLEncoder.encode(k,"utf-8")+"="+URLEncoder.encode(params.get(k),"utf-8") );
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		URL addr=new URL(b.toString());
		Object res=addr.getContent();
		if (res instanceof InputStream) {
			InputStream in = (InputStream) res;
			XmlParser p=new XmlParser();
			try {
				Node res2=p.parse(in);
				return res2;
			} catch (SAXException e) {
				e.printStackTrace();
			}
			
			/*BufferedReader r=new BufferedReader(new InputStreamReader(in));
			while (true) {
				String s=r.readLine();
				if (s==null) break;
				System.out.println(s);
			}
			r.close();*/
		}
		System.out.println("ERR - url is "+addr);
		return null;
	}
	public static void main(String[] args) throws MalformedURLException, IOException {
		
	}
	public WebClient(String url, Map<String, String> params) {
		super();
		this.url = url;
		this.params = params;
	}
}
