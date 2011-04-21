package jp.tonyu.soytext2.servlet;

import java.io.File;
import java.io.IOException;

import jp.tonyu.nanoservlet.NanoServlet;
import jp.tonyu.soytext2.db.SDB;
import jp.tonyu.soytext2.js.DocumentLoader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SMain extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		doIt(req,res);
	}
	private void doIt(HttpServletRequest req, HttpServletResponse res) {
		try {
			new HttpContext(loader, req, res).proc();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		doIt(req,res);
	}
	SDB sdb;
	DocumentLoader loader;
	public SMain() throws Exception{
		sdb=new SDB(new File("test.db"));
		loader=new DocumentLoader(sdb);
		NanoServlet n=new NanoServlet(3002, this);
		System.out.println( "Listening on port 3002. Hit Enter to stop.\n" );
		try { System.in.read(); } catch( Throwable t ) {};		
		sdb.close();
	}
	public static void main(String[] args) throws Exception {
		new SMain();
	}
}