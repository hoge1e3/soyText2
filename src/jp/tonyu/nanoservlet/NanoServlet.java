package jp.tonyu.nanoservlet;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public class NanoServlet extends NanoHTTPD {
	HttpServlet servlet;
	/**
	 * Run HttpServlet on NanoHTTPD
	 * 
	 * @param port   Port number
	 * @param servlet  servlet which runs on NanoHTTPD
	 * @throws IOException
	 */
	public NanoServlet(int port, HttpServlet servlet) throws IOException {
		super(port);
		this.servlet=servlet;
	}
	@Override
	public Response serve(String uri, String method, Properties header,
			Properties parms, Properties files) {
		HttpServletRequest req=new RequestWrapper(uri, method,	 header, parms);
		ResponseWrapper res = new ResponseWrapper(this);
		try {
			servlet.service(req, res);
		} catch (ServletException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res.close();
		
	}
}
