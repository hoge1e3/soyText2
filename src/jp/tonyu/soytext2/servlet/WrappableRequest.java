package jp.tonyu.soytext2.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import jp.tonyu.js.Wrappable;

public class WrappableRequest implements HttpServletRequest, Wrappable {
	HttpServletRequest req;

	public WrappableRequest(HttpServletRequest req) {
		super();
		this.req = req;
	}

	public Object getAttribute(String arg0) {
		return req.getAttribute(arg0);
	}

	public Enumeration getAttributeNames() {
		return req.getAttributeNames();
	}

	public String getAuthType() {
		return req.getAuthType();
	}

	public String getCharacterEncoding() {
		return req.getCharacterEncoding();
	}

	public int getContentLength() {
		return req.getContentLength();
	}

	public String getContentType() {
		return req.getContentType();
	}

	public String getContextPath() {
		return req.getContextPath();
	}

	public Cookie[] getCookies() {
		return req.getCookies();
	}

	public long getDateHeader(String arg0) {
		return req.getDateHeader(arg0);
	}

	public String getHeader(String arg0) {
		return req.getHeader(arg0);
	}

	public Enumeration getHeaderNames() {
		return req.getHeaderNames();
	}

	public Enumeration getHeaders(String arg0) {
		return req.getHeaders(arg0);
	}

	public ServletInputStream getInputStream() throws IOException {
		return req.getInputStream();
	}

	public int getIntHeader(String arg0) {
		return req.getIntHeader(arg0);
	}

	public String getLocalAddr() {
		return req.getLocalAddr();
	}

	public String getLocalName() {
		return req.getLocalName();
	}

	public int getLocalPort() {
		return req.getLocalPort();
	}

	public Locale getLocale() {
		return req.getLocale();
	}

	public Enumeration getLocales() {
		return req.getLocales();
	}

	public String getMethod() {
		return req.getMethod();
	}

	public String getParameter(String arg0) {
		return req.getParameter(arg0);
	}

	public Map getParameterMap() {
		return req.getParameterMap();
	}

	public Enumeration getParameterNames() {
		return req.getParameterNames();
	}

	public String[] getParameterValues(String arg0) {
		return req.getParameterValues(arg0);
	}

	public String getPathInfo() {
		return req.getPathInfo();
	}

	public String getPathTranslated() {
		return req.getPathTranslated();
	}

	public String getProtocol() {
		return req.getProtocol();
	}

	public String getQueryString() {
		return req.getQueryString();
	}

	public BufferedReader getReader() throws IOException {
		return req.getReader();
	}

	public String getRealPath(String arg0) {
		return req.getRealPath(arg0);
	}

	public String getRemoteAddr() {
		return req.getRemoteAddr();
	}

	public String getRemoteHost() {
		return req.getRemoteHost();
	}

	public int getRemotePort() {
		return req.getRemotePort();
	}

	public String getRemoteUser() {
		return req.getRemoteUser();
	}

	public RequestDispatcher getRequestDispatcher(String arg0) {
		return req.getRequestDispatcher(arg0);
	}

	public String getRequestURI() {
		return req.getRequestURI();
	}

	public StringBuffer getRequestURL() {
		return req.getRequestURL();
	}

	public String getRequestedSessionId() {
		return req.getRequestedSessionId();
	}

	public String getScheme() {
		return req.getScheme();
	}

	public String getServerName() {
		return req.getServerName();
	}

	public int getServerPort() {
		return req.getServerPort();
	}

	public String getServletPath() {
		return req.getServletPath();
	}

	public HttpSession getSession() {
		return req.getSession();
	}

	public HttpSession getSession(boolean arg0) {
		return req.getSession(arg0);
	}

	public Principal getUserPrincipal() {
		return req.getUserPrincipal();
	}

	public boolean isRequestedSessionIdFromCookie() {
		return req.isRequestedSessionIdFromCookie();
	}

	public boolean isRequestedSessionIdFromURL() {
		return req.isRequestedSessionIdFromURL();
	}

	public boolean isRequestedSessionIdFromUrl() {
		return req.isRequestedSessionIdFromUrl();
	}

	public boolean isRequestedSessionIdValid() {
		return req.isRequestedSessionIdValid();
	}

	public boolean isSecure() {
		return req.isSecure();
	}

	public boolean isUserInRole(String arg0) {
		return req.isUserInRole(arg0);
	}

	public void removeAttribute(String arg0) {
		req.removeAttribute(arg0);
	}

	public void setAttribute(String arg0, Object arg1) {
		req.setAttribute(arg0, arg1);
	}

	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {
		req.setCharacterEncoding(arg0);
	}
	
}
