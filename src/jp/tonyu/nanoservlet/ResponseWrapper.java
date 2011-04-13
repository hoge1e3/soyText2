package jp.tonyu.nanoservlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import jp.tonyu.nanoservlet.NanoHTTPD.Response;
import jp.tonyu.soytext2.js.Wrappable;


public class ResponseWrapper implements HttpServletResponse,Wrappable {
	NanoHTTPD.Response res;
	ServletOutputStream out;
	public void notimpl() {
		throw new RuntimeException("Not implemented");
	}
	public ResponseWrapper(NanoHTTPD nano) {
		super();
		this.res = nano.newResponse();
	}
	public Response close() {
		try {
			if (writer!=null)	writer.close();
			else if (out!=null)	out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}
	@Override
	public void addCookie(Cookie c) {
		// Known bug: cannot add two or more cookies.
		res.addHeader("Set-Cookie", c.getName()+"="+c.getValue()+
				"; expires=Wed, 10-Jun-2020 12:00:00 GMT; path=/;");
	}

	@Override
	public void addDateHeader(String arg0, long arg1) {
		notimpl();
		
	}

	@Override
	public void addHeader(String arg0, String arg1) {
		notimpl();
		
	}

	@Override
	public void addIntHeader(String arg0, int arg1) {
		notimpl();
		
	}

	@Override
	public boolean containsHeader(String arg0) {
		notimpl();
		return false;
	}

	@Override
	public String encodeRedirectURL(String arg0) {
		notimpl();
		return null;
	}

	@Override
	public String encodeRedirectUrl(String arg0) {
		notimpl();
		return null;
	}

	@Override
	public String encodeURL(String arg0) {
		notimpl();
		return null;
	}

	@Override
	public String encodeUrl(String arg0) {
		notimpl();
		return null;
	}

	@Override
	public void sendError(int arg0) throws IOException {
		notimpl();
		
	}

	@Override
	public void sendError(int arg0, String arg1) throws IOException {
		notimpl();
		
	}

	@Override
	public void sendRedirect(String value) throws IOException {
		res.header.put("Location", value);
		setStatus(301);
	}

	@Override
	public void setDateHeader(String arg0, long arg1) {
		notimpl();
		
	}

	@Override
	public void setHeader(String key, String value) {
		res.header.put(key, value);
	}

	@Override
	public void setIntHeader(String arg0, int arg1) {
		notimpl();
		
	}
/*HTTP_OK = "200 OK",
		HTTP_REDIRECT = "301 Moved Permanently",
		HTTP_FORBIDDEN = "403 Forbidden",
		HTTP_NOTFOUND = "404 Not Found",
		HTTP_BADREQUEST = "400 Bad Request",
		HTTP_INTERNALERROR = "500 Internal Server Error",
		HTTP_NOTIMPLEMENTED = "501 Not Implemented";*/
	@Override
	public void setStatus(int arg0) {
		switch (arg0) {
		case 200: res.status=NanoHTTPD.HTTP_OK;break;
		case 301: res.status=NanoHTTPD.HTTP_REDIRECT;break;
		case 403: res.status=NanoHTTPD.HTTP_FORBIDDEN;break;
		case 404: res.status=NanoHTTPD.HTTP_NOTFOUND;break;
		case 400: res.status=NanoHTTPD.HTTP_BADREQUEST;break;
		case 500: res.status=NanoHTTPD.HTTP_INTERNALERROR;break;
		case 501: res.status=NanoHTTPD.HTTP_NOTIMPLEMENTED;break;		
		}
	}

	@Override
	public void setStatus(int arg0, String arg1) {
		notimpl();
		
	}

	@Override
	public void flushBuffer() throws IOException {
		notimpl();
		
	}

	@Override
	public int getBufferSize() {
		notimpl();
		return 0;
	}

	@Override
	public String getCharacterEncoding() {
		notimpl();
		return null;
	}

	@Override
	public String getContentType() {
		notimpl();
		return null;
	}

	@Override
	public Locale getLocale() {
		notimpl();
		return null;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (out!=null) return out;
		return out=new ServletOutputStream() {
			ByteArrayOutputStream bout=new ByteArrayOutputStream();
			@Override
			public void write(byte[] b, int off, int len) {
				//System.out.println("Write!");
				bout.write(b, off, len);
			}

			@Override
			public void write(byte[] b) throws IOException {
				//System.out.println("Write2!");
				bout.write(b);
			}

			@Override
			public void write(int b) {
				//System.out.println("Write3!");
				bout.write(b);
			}
			

			@Override
			public void close() throws IOException {
				super.close();
				byte[] byteArray = bout.toByteArray();
				res.data=new ByteArrayInputStream(byteArray);
				System.out.println("data set . len="+byteArray.length);
			}
		};
	}
	PrintWriter writer;
	@Override
	public PrintWriter getWriter() throws IOException {
		if (writer!=null) return writer;
		return writer=new PrintWriter(new OutputStreamWriter(getOutputStream(),"utf-8"));
	}

	@Override
	public boolean isCommitted() {
		notimpl();
		return false;
	}

	@Override
	public void reset() {
		notimpl();
		
	}

	@Override
	public void resetBuffer() {
		notimpl();
		
	}

	@Override
	public void setBufferSize(int arg0) {
		notimpl();
		
	}

	@Override
	public void setCharacterEncoding(String arg0) {
		notimpl();
		
	}

	@Override
	public void setContentLength(int arg0) {
		notimpl();
		
	}

	@Override
	public void setContentType(String value) {
		//res.header.put("Content-Type", value);
		res.mimeType=value;
	}

	@Override
	public void setLocale(Locale arg0) {
		notimpl();
		
	}
	
}
