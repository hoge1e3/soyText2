package jp.tonyu.soytext2.servlet;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.tonyu.debug.Log;
import jp.tonyu.nanoservlet.AutoRestart;
import jp.tonyu.nanoservlet.NanoServlet;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.js.DocumentLoader;
import jp.tonyu.soytext2.js.JSSession;
import jp.tonyu.util.Ref;
import jp.tonyu.util.ResourceTraverser;
import jp.tonyu.util.SFile;

import java.io.InputStream;
import org.tmatesoft.sqljet.core.SqlJetException;

public class SMain extends HttpServlet {
	public static final String DB_INIT_PATH = "jp/tonyu/soytext2/servlet/init/db";
	private static final String KEY_DOCLOADER = "__Document_LOADER";
	//JSSession j=new JSSession();
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		doIt(req,res);
	}
	private void doIt(final HttpServletRequest req2, final HttpServletResponse res2) {
		final DocumentLoader docLoader;
		final HttpServletRequest req=new WrappableRequest(req2);
		final HttpServletResponse res=new WrappableResponse(res2);
		try {
			initServlet();
		} catch (SqlJetException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HttpSession s=req2.getSession();
		Object jsl=s.getAttribute(KEY_DOCLOADER);
		if (!(jsl instanceof DocumentLoader)) {
			jsl=new DocumentLoader(sdb);
			s.setAttribute(KEY_DOCLOADER, jsl);
		}
		docLoader=(DocumentLoader)jsl;
		/*if (docLoader==null) {
			docLoader=new DocumentLoader(sdb);
		}*/
		JarDownloader.jarFile.enter(jarFile, new Runnable() {
			@Override
			public void run() {
				DocumentLoader.cur.enter(docLoader, new Runnable() {
					@Override
					public void run() {
						try {
							new HttpContext(DocumentLoader.cur.get(), req, res).proc();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			}
		});
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		doIt(req,res);
	}
	SDB sdb;
	String jarFile;
	//DocumentLoader loader;
	public  File getNewestDBFile() throws IOException {
		long max=0;
		File res=null;
		SFile dbDir =  dbDir();
		SFile dir;
		SFile pid=dbDir.rel(SDB.PRIMARY_DBID_TXT);
		if (pid.exists()) {
			dir=dbDir.rel(pid.text());
		} else {
			dir=dbDir;
		}
		for (SFile d:dir) {
			if (!d.name().endsWith(".db")) continue;
			long l=d.lastModified();
			if (l>max) {
				res=d.javaIOFile();
				max=l;
			}
		}
		/*if (res==null) {
			return dbDir.rel("main.db").javaIOFile();
		}*/
		return res;
	}
	private SFile dbDir() {
		return workspaceDir.rel("db/");
	}
	String detectWorkSpace(String path) {
		String[] ws=path.split(";");
		String res=null;
		for (String c:ws) {
			res=c;
			if (new File(c).exists()) return c;
		}
		throw new RuntimeException("No file in "+path);
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
	public void initServlet() throws SqlJetException,IOException {
		if (!isServlet || servletInited) return;
		servletInited=true;
		setupApplicationContext();
		String jarfileP = getServletContext().getInitParameter("jarFile");
		if (jarfileP!=null) jarFile=detectWorkSpace(jarfileP  );
		File newest = getNewestDBFile();
		if (newest==null) Log.die("Error no db file exitst in "+dbDir());
		System.out.println("Using "+newest+" as db.");
		sdb=new SDB(newest);//, SDB.UID_EXISTENT_FILE);
		//loader=new DocumentLoader(sdb);
	}
	File setupDB() throws IOException {
		final SFile dbDir=dbDir();
		ClassLoader cl=this.getClass().getClassLoader();
		//SFile dbIdFile=dbDir.rel(SDB.PRIMARY_DBID_TXT);
		InputStream in=cl.getResourceAsStream(DB_INIT_PATH+"/"+SDB.PRIMARY_DBID_TXT);
		Scanner s=new Scanner(in);
		String dbid=s.nextLine();
		s.close();
		
		//dbIdFile.readFrom(in);
		//String dbId=dbIdFile.text();		
		SFile dbDir_in=dbDir.rel(dbid);		
		SFile dbFile=dbDir_in.rel("main.db");
		if (!dbFile.exists()) {
			in=cl.getResourceAsStream(DB_INIT_PATH+"/"+"main.db");
			dbFile.readFrom(in);
		}
		return dbFile.javaIOFile();
		/*ResourceTraverser r=new ResourceTraverser() {
			
			@Override
			protected void visitFile(String name) throws IOException {
				String rel=name.substring(DB_INIT_PATH.length()+1);
				SFile f=dbDir.rel(rel);
				System.out.println(name + "->" + f);
				InputStream in = getInputStream(name);
				f.readFrom(in);
				in.close();
			}
			@Override
			protected boolean visitDir(String name, List<String> files)
					throws IOException {
				System.out.println("dir:"+name);
				return false;
			}
			@Override
			protected boolean isDir(String name) {
				if (name.startsWith(DB_INIT_PATH) &&
						(name.endsWith(".txt") || name.endsWith(".db"))) return false;
				return super.isDir(name);
			}
		};*/
		//r.traverse(".");//DB_INIT_PATH);
		//r.traverse("jp/tonyu/db/DBAction.class");//DB_INIT_PATH);
		//Log.die("Die");
		//return getNewest();
	}
	// As Application
	public SMain(int port) throws Exception{
		workspaceDir=new SFile(new File("."));
		File newest = getNewestDBFile();
		if (newest==null) newest=setupDB();
		System.out.println("Using "+newest+" as db.");
		sdb=new SDB(newest);//, uid);
		jarFile="";
		//loader=new DocumentLoader(sdb);
		//int port = 3002;
		AutoRestart auto = new AutoRestart(port, workspaceDir.rel("stop.lock").javaIOFile());
		NanoServlet n=new NanoServlet(port, this, auto);
		System.out.println("Listening on port "+port+". Go to "+auto.stopURL()+" to stop.\n" );
		final Ref<Boolean> stop=Ref.create(false);
		Log.showLogWindow(new Runnable() {
			public void run() {
				stop.set(true);				
			}
		});
		String openurl = "http://localhost:"+port+"/?"+Math.random();
		Log.d("OPEN", openurl);
		Desktop desktop = Desktop.getDesktop();
        desktop.browse(new URI(openurl));

 		while (stop.get()==false) {
 			Thread.sleep(1000);
 			if (n.hasToBeStopped()) break;
 		}
 		//try { System.in.read(); } catch( Throwable t ) {};		
		sdb.close();
		n.stop();
		System.exit(1);
	}
	public static void main(String[] args) throws Exception {
		int port=3010;
		if (args.length>0) {
			//System.err.println("Usage: java SMain port");
			//System.err.println("uid must be world-unique id such as UUID, time.domain or some string that google returns 0 results.");
			//return;
			port=Integer.parseInt(args[0]);
		}
		new SMain(port );
	}
}
