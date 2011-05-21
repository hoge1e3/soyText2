package jp.tonyu.soytext2.servlet;

import java.io.File;
import java.io.IOException;

import jp.tonyu.nanoservlet.NanoServlet;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.js.DocumentLoader;
import jp.tonyu.soytext2.js.JSSession;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SMain extends HttpServlet {
	JSSession j=new JSSession();
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		doIt(req,res);
	}
	private void doIt(final HttpServletRequest req, final HttpServletResponse res) {
		JSSession.cur.enter(j, new Runnable() {

			@Override
			public void run() {
				try {
					new HttpContext(loader, req, res).proc();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		doIt(req,res);
	}
	SDB sdb;
	DocumentLoader loader;
	public SMain() throws Exception{
		sdb=new SDB(new File("main.db"));
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
