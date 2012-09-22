package jp.tonyu.nanoservlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.file.FileProperty;


public class RequestWrapper implements HttpServletRequest,Wrappable,FileProperty {
	String uri;
	String method;
	Properties header;
	Properties parms;
	Properties files;
	@Override
	public Properties getFiles() {
	    return files;
	}
	@Override
	public Properties getParams() {
	    return parms;
	}
	public RequestWrapper(String uri, String method, Properties header,
			Properties parms, Properties files) {
		super();
		this.uri = uri;
		this.method = method;
		this.header = header;
		this.parms = parms;
		this.files = files;
	}
	public void notimpl() {
		throw new RuntimeException("Not implemented");
	}
	@Override
	public String getAuthType() {
		notimpl();
		return null;
	}

	@Override
	public String getContextPath() {
		notimpl();
		return null;
	}

	@Override
	public Cookie[] getCookies() {
		String c=header.getProperty("cookie");
		if (c==null) return new Cookie[0];
		String[] cs = c.split(";");
		Cookie[] res=new Cookie[cs.length];
		int i=0;
		for (String e:cs) {
			String[] kv=e.split("=");
			res[i]=new Cookie(kv[0].trim(), kv[1].trim());
			i++;
		}
		return res;
	}

	@Override
	public long getDateHeader(String arg0) {
		// TODO: いいかげん
		return 0;
	}

	@Override
	public String getHeader(String arg0) {
		return header.getProperty(arg0);
	}

	@Override
	public Enumeration getHeaderNames() {
		notimpl();
		return null;
	}

	@Override
	public Enumeration getHeaders(String arg0) {
		notimpl();
		return null;
	}

	@Override
	public int getIntHeader(String arg0) {
		notimpl();
		return 0;
	}

	@Override
	public String getMethod() {
		return method;
	}

	@Override
	public String getPathInfo() {
		return uri;
	}

	@Override
	public String getPathTranslated() {
		notimpl();
		return null;
	}

	@Override
	public String getQueryString() {
		return getParameter(NanoHTTPD.ENTIRE_QUERY_STRING);
	}

	@Override
	public String getRemoteUser() {
		notimpl();
		return null;
	}

	@Override
	public String getRequestURI() {
		notimpl();
		return null;
	}

	@Override
	public StringBuffer getRequestURL() {
		return new StringBuffer(uri);
	}

	@Override
	public String getRequestedSessionId() {
		notimpl();
		return null;
	}

	@Override
	public String getServletPath() {
		notimpl();
		return null;
	}

	@Override
	public HttpSession getSession() {
		return SingletonHttpSession.inst;
	}

	@Override
	public HttpSession getSession(boolean arg0) {
		notimpl();
		return null;
	}

	@Override
	public Principal getUserPrincipal() {
		notimpl();
		return null;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		notimpl();
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		notimpl();
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		notimpl();
		return false;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		notimpl();
		return false;
	}

	@Override
	public boolean isUserInRole(String arg0) {
		notimpl();
		return false;
	}

	@Override
	public Object getAttribute(String arg0) {
		notimpl();
		return null;
	}

	@Override
	public Enumeration getAttributeNames() {
		notimpl();
		return null;
	}

	@Override
	public String getCharacterEncoding() {
		notimpl();
		return null;
	}

	@Override
	public int getContentLength() {
		notimpl();
		return 0;
	}

	@Override
	public String getContentType() {
		return getHeader("content-type");
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		notimpl();
		return null;
	}

	@Override
	public String getLocalAddr() {
		notimpl();
		return null;
	}

	@Override
	public String getLocalName() {
		notimpl();
		return null;
	}

	@Override
	public int getLocalPort() {
		notimpl();
		return 0;
	}

	@Override
	public Locale getLocale() {
		notimpl();
		return null;
	}

	@Override
	public Enumeration getLocales() {
		notimpl();
		return null;
	}

	@Override
	public String getParameter(String arg0) {
		return parms.getProperty(arg0);
	}

	@Override
	public Map getParameterMap() {
		Map res=new Hashtable();
		for (Object key:parms.keySet()) {
			if (NanoHTTPD.ENTIRE_QUERY_STRING.equals(key)) continue;
			String value=parms.getProperty((String)key);
			res.put(key, new String[]{value});
		}
		return res;
	}

	@Override
	public Enumeration getParameterNames() {
		notimpl();
		return null;
	}

	@Override
	public String[] getParameterValues(String arg0) {
		notimpl();
		return null;
	}

	@Override
	public String getProtocol() {
		notimpl();
		return null;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		notimpl();
		return null;
	}

	@Override
	public String getRealPath(String arg0) {
		notimpl();
		return null;
	}

	@Override
	public String getRemoteAddr() {
		//notimpl();
		return "localhost";//header.getProperty(NanoHTTPD.REMOTE_ADDR);
	}

	@Override
	public String getRemoteHost() {
		//notimpl();
		return "localhost";//header.getProperty(NanoHTTPD.REMOTE_HOST);
	}

	@Override
	public int getRemotePort() {
		notimpl();
		return 0;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		notimpl();
		return null;
	}

	@Override
	public String getScheme() {
		notimpl();
		return null;
	}

	@Override
	public String getServerName() {
		notimpl();
		return null;
	}

	@Override
	public int getServerPort() {
		notimpl();
		return 0;
	}

	@Override
	public boolean isSecure() {
		notimpl();
		return false;
	}

	@Override
	public void removeAttribute(String arg0) {
		notimpl();

	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		notimpl();

	}

	@Override
	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {
		//TODO UTF-8固定
	}

}
