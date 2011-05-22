package jp.tonyu.soytext2.servlet;

import java.io.File;
import java.io.IOException;

import jp.tonyu.nanoservlet.AutoRestart;
import jp.tonyu.nanoservlet.NanoServlet;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.js.DocumentLoader;
import jp.tonyu.soytext2.js.JSSession;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.sqljet.core.SqlJetException;

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
		int port = 3002;
		AutoRestart auto = new AutoRestart(port, new File("stop.lock"));
		NanoServlet n=new NanoServlet(port, this, auto);
		System.out.println( "Listening on port "+port+". Go to "+auto.stopURL()+" to stop.\n" );
 		while (true) {
 			Thread.sleep(1000);
 			if (n.hasToBeStopped()) break;
 		}
 		//try { System.in.read(); } catch( Throwable t ) {};		
		sdb.close();
		n.stop();
	}
	public static void main(String[] args) throws Exception {
		new SMain();
	}
}
