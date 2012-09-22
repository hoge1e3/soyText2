package jp.tonyu.soytext2.servlet;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.tonyu.debug.Log;
import jp.tonyu.js.Wrappable;
import jp.tonyu.nanoservlet.AutoRestart;
import jp.tonyu.nanoservlet.NanoServlet;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.js.DocumentLoader;
import jp.tonyu.util.Ref;
import jp.tonyu.util.SFile;

public class SMain extends HttpServlet {

	private static final String AUTH2 = "AUTH";
	private static final String KEY_DOCLOADER = "__Document_LOADER";
	//JSSession j=new JSSession();
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		doIt(req,res);
	}

	private void doIt(final HttpServletRequest req2, final HttpServletResponse res2) {
		final HttpServletRequest req=wrapRequest(req2);
		final HttpServletResponse res=wrapResponse(res2);
		try {
			initServlet();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
		//HttpSession s=req2.getSession();
		/*For one docloader per session
		final DocumentLoader docLoader;
		Object jsl=s.getAttribute(KEY_DOCLOADER);
		if (!(jsl instanceof DocumentLoader)) {
			jsl=new DocumentLoader(sdb);
			s.setAttribute(KEY_DOCLOADER, jsl);
		}
		docLoader=(DocumentLoader)jsl;*/
		// For single docloader
		if (docLoader==null) {
			docLoader=new DocumentLoader(sdb);
		}
		HttpSession s = req2.getSession();
		Object aa=s.getAttribute(AUTH2);
		Auth auth;
		if (aa instanceof Auth) {
			auth=(Auth)aa;
		} else {
			auth=new Auth(docLoader.authenticator());
			s.setAttribute(AUTH2, auth);
		}
		Auth.cur.enter(auth, new Runnable() {
			@Override
			public void run() {
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
		});
	}

    private HttpServletResponse wrapResponse(final HttpServletResponse res2) {
        if (res2 instanceof Wrappable) {
            return res2;
        }
        return new WrappableResponse(res2);
    }

    private HttpServletRequest wrapRequest(final HttpServletRequest req2) {
        if (req2 instanceof Wrappable) {
            return req2;
        }
        return new WrappableRequest(req2);
    }
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		doIt(req,res);
	}
	SDB sdb;
	String jarFile;
	DocumentLoader docLoader;
	/*public  File getNewestDBFile() throws IOException {
		return getNewestPrimaryDBFile(dbDir());
	}
	public static File getNewestPrimaryDBFile(SFile dbDir) throws IOException {
		long max=0;
		File res=null;

		SFile dir = getPrimaryDBDir(dbDir);
		Log.d("getNewestDB", "Search in "+dir);
		for (SFile d:dir) {
			Log.d("getNewestDB", d);
			if (!d.name().endsWith(".db")) continue;
			long l=d.lastModified();
			if (l>max) {
				res=d.javaIOFile();
				max=l;
			}
		}
		//if (res==null) {
		//return dbDir.rel("main.db").javaIOFile();
		//}
		return res;
	}
	public static SFile getPrimaryDBDir(SFile dbDir) throws IOException {
		SFile dir;
		SFile pid=dbDir.rel(SDB.PRIMARY_DBID_TXT);
		if (pid.exists()) {
			dir=dbDir.rel(pid.text());
		} else {
			dir=dbDir;
		}
		return dir;
	}*/
	/*private SFile dbDir() {
		return workspaceDir.rel("db/");
	}*/
	String detectPath(String path) {
		String[] ws=path.split(";");
		String res=null;
		for (String c:ws) {
			res=c;
			if (new File(c).exists()) {
				Log.d("SMain", "Workspace="+c);
				return c;
			}
		}
		throw new RuntimeException("No file in "+path);
	}
	//String workspace;
	//SFile workspaceDir;
	Workspace workspace;
	synchronized void setupApplicationContext() {
		if (workspace==null) {
			workspace=new Workspace( new SFile( detectPath(  getServletContext().getInitParameter("workspace") ) ));
			//workspaceDir=new SFile(workspace);
		}
	}
	boolean isServlet=false;
	boolean servletInited=false;
	public static int insts=0;
	// As Servlet
	public SMain() {insts++; isServlet=true;}
	public void initServlet() throws SQLException,IOException, ClassNotFoundException {
		if (!isServlet || servletInited) return;
		servletInited=true;
		setupApplicationContext();
		String jarfileP = getServletContext().getInitParameter("jarFile");
		if (jarfileP!=null) jarFile=detectPath(jarfileP  );
		/*File newest =  getNewestDBFile();
		if (newest==null) Log.die("Error no db file exitst in "+dbDir());
		System.out.println("Using "+newest+" as db.");*/
		sdb= workspace.getPrimaryDB(); // new SDB(newest);//, SDB.UID_EXISTENT_FILE);
		System.out.println("Using "+sdb+" as db.");
		//loader=new DocumentLoader(sdb);
	}
	/*File setupDB() throws IOException {
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
	}*/
	// As Application
	public SMain(int port) throws Exception{
		workspace=new Workspace(new SFile("soytext"));
		//workspaceDir=new SFile(new File("."));
		workspace.setupDB();
		//File newest = getNewestDBFile();
		//if (newest==null) newest=setupDB();
		sdb=workspace.getPrimaryDB();//  new SDB(newest);//, uid);
		System.out.println("Using "+sdb+" as db.");
		jarFile="";
		//loader=new DocumentLoader(sdb);
		//int port = 3002;
		AutoRestart auto = new AutoRestart(port, workspace.home.rel("stop.lock").javaIOFile());
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
