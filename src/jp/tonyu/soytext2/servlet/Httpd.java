package jp.tonyu.soytext2.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;



public class Httpd {
    public static void respondByString(HttpServletResponse response, String str) throws IOException
    {
    	response.getWriter().println(str);
    }
    public static void respondByFile(HttpServletResponse response, String fileName) throws IOException
    {
    }
}
