package jp.tonyu.soytext2.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.tonyu.nanoservlet.AutoRestart;
import jp.tonyu.nanoservlet.NanoServlet;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.js.DocumentLoader;
import jp.tonyu.soytext2.js.JSSession;
import jp.tonyu.util.SFile;

import org.tmatesoft.sqljet.core.SqlJetException;

public class SMain extends HttpServlet {
	JSSession j=new JSSession();
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		doIt(req,res);
	}
	private void doIt(final HttpServletRequest req2, final HttpServletResponse res2) {
		final HttpServletRequest req=new WrappableRequest(req2);
		final HttpServletResponse res=new WrappableResponse(res2);
		try {
			initServlet();
		} catch (SqlJetException e1) {
			e1.printStackTrace();
		}
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
	public  File getNewest() {
		long max=0;
		File res=null;
		SFile dbDir =  workspaceDir.rel("db/");
		for (SFile d:dbDir) {
			if (!d.name().endsWith(".db")) continue;
			long l=d.lastModified();
			if (l>max) {
				res=d.javaIOFile();
				max=l;
			}
		}
		if (res==null) {
			return dbDir.rel("main.db").javaIOFile();
		}
		return res;
	}
	String detectWorkSpace(String path) {
		String[] ws=path.split(";");
		String res=null;
		for (String c:ws) {
			res=c;
			if (new File(c).exists()) return c;
		}
		throw new RuntimeException("No workspace in "+path);
	}
	String workspace;
	SFile workspaceDir;
	synchronized void setupApplicationContext() {
		if (workspace==null) {
			workspace=detectWorkSpace(  getServletContext().getInitParameter("workspace") );
			workspaceDir=new SFile(workspace);
		}
	}
	boolean isServlet=false;
	boolean servletInited=false;
	public static int insts=0;
	// As Servlet
	public SMain() {insts++; isServlet=true;}
	public void initServlet() throws SqlJetException {
		if (!isServlet || servletInited) return;
		servletInited=true;
		setupApplicationContext();
		File newest = getNewest();
		System.out.println("Using "+newest+" as db.");
		sdb=new SDB(newest, SDB.UID_EXISTENT_FILE);
		loader=new DocumentLoader(sdb);
	}
	// As Application
	public SMain(String uid) throws Exception{
		workspaceDir=new SFile(new File("."));
		File newest = getNewest();
		System.out.println("Using "+newest+" as db.");
		sdb=new SDB(newest, uid);
		loader=new DocumentLoader(sdb);
		int port = 3002;
		AutoRestart auto = new AutoRestart(port, workspaceDir.rel("stop.lock").javaIOFile());
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
		if (args.length==0) {
			System.err.println("Usage: java SMain uid");
			System.err.println("uid must be world-unique id such as UUID, time.domain or some string that google returns 0 results.");
			return;
		}
		new SMain(args[0]);
	}
}
