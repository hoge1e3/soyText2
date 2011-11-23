package jp.tonyu.util;

import java.util.Scanner;

import jp.tonyu.soytext2.js.HashLiteralConv;

public class Resource {
	public static String text(Class klass,String ext) {
		Scanner in= new Scanner(
				klass.getResourceAsStream(klass.getSimpleName()+ext)
				);
		StringBuilder buf=new StringBuilder();
		while (in.hasNextLine()) {
			buf.append(in.nextLine()+"\n");
		}
		in.close();
		return buf+"";
	}
	
}
