package jp.tonyu.soytext2.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;



public class Httpd {
    public static void respondByString(HttpServletResponse response, String str) throws IOException
    {
    	response.getWriter().println(str);
    }
    public static void respondByFile(HttpServletResponse response, File file) throws IOException
    {
    	String con=HttpContext.detectContentType(file.getName());
    	response.setContentType(con);
    	FileInputStream in = new FileInputStream(file);
		outputFromStream(response, in);
		in.close();
    }
    public static void outputFromStream(HttpServletResponse res, InputStream in) throws IOException {
    	byte[] buf= new byte[1024];
    	while (true) {
    		int r=in.read(buf);
    		if (r<=0) break;
    		res.getOutputStream().write(buf,0,r);
    	}
    }

}
