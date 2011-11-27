package jp.tonyu.soytext2.servlet;

import static jp.tonyu.util.Literal.toLiteral;
import static jp.tonyu.util.SPrintf.sprintf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.tonyu.debug.Log;
import jp.tonyu.js.Args;
import jp.tonyu.js.BuiltinFunc;
import jp.tonyu.js.ContextRunnable;
import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.auth.Authenticator;
import jp.tonyu.soytext2.auth.AuthenticatorList;
import jp.tonyu.soytext2.auth.Session;
import jp.tonyu.soytext2.auth.SessionSet;
import jp.tonyu.soytext2.browserjs.IndentAdaptor;
import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.document.DocumentSet;
import jp.tonyu.soytext2.document.backup.Importer;
import jp.tonyu.soytext2.js.ContentChecker;
import jp.tonyu.soytext2.js.DocumentLoader;
import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.js.JSSession;
import jp.tonyu.soytext2.search.Query;
import jp.tonyu.soytext2.search.QueryBuilder;
import jp.tonyu.soytext2.search.expr.AttrOperator;
import jp.tonyu.util.HttpPost;
import jp.tonyu.util.Literal;
import jp.tonyu.util.MapAction;
import jp.tonyu.util.Maps;
import jp.tonyu.util.Ref;
import jp.tonyu.util.Resource;
import jp.tonyu.util.SPrintf;
import jp.tonyu.util.Util;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.tmatesoft.sqljet.core.SqlJetException;

public class HttpContext implements Wrappable {
	public static final String CONTENT_TYPE = "Content-Type";
	private static final String DATA = "data";
	//private static final String SYNCID = "syncid";
	private static final String DOWNLOADSINCE = "downloadsince";
	private static final String DO_EDIT = "doEdit";
	public static final String TEXT_PLAIN_CHARSET_UTF_8 = "text/plain; charset=utf-8";
	private static final String TEXT_HTML_CHARSET_UTF_8 = "text/html; charset=utf-8";
	private static final String SEL = "sel_";
	public static final jp.tonyu.util.Context<HttpContext> cur=new jp.tonyu.util.Context<HttpContext>();
	private static final String SESSION_NAME = "soyText_Session";
	/*public final soytext.script.Context context= new soytext.script.Context(true);
	public SessionManager sessionManager() {
		return appCtx.sessionManager;
	}*/
	public boolean isRoot() {
		String addr=req.getRemoteAddr();
		if ("localhost".equals(addr) || "127.0.0.1".equals(addr) || "0:0:0:0:0:0:0:1".equals(addr)) {
			return true;
		}
		String user=currentSession().userName();
		if (documentLoader.authenticator().isRootUser(user)) return true;
		return false;
	}
	public boolean assertRoot() {
		if (isRoot()) return false;
		redirect(rootPath()+"/auth");
		return true;
	}
	Session currentSession=null;
	public Session currentSession() {
		if (currentSession!=null) return currentSession;
		if (req.getCookies()!=null) {
			for (Cookie c:req.getCookies()) {
				if (c.getName().equals(SESSION_NAME)) {
					currentSession= SessionSet.get(c.getValue());
					if (currentSession!=null) return currentSession;
					break;
				}
			}
		}
		return currentSession=Session.NOBODY;
	}
	/*public ApplicationContext applicationContext() {
		return currentSession().applicationContext();
	}*/
	public final DocumentLoader documentLoader;
	public DocumentSet documentSet() {
		/*Session s= currentSession();
		if (s==null) return appCtx.documentSet;
		return s.documentSet();*/
		return documentLoader.getDocumentSet();
	}
	public HttpContext( DocumentLoader loader, HttpServletRequest req, HttpServletResponse res) {
		super();
		this.req = req;
		this.res = res;
		this.documentLoader=loader;
	}
	int printc=0;
	public void print(Object str) throws IOException { 
		res.getWriter().print(str);		
		printc++;
	}
	public int getPrintCount(){return printc;}
	
	private HttpServletRequest req;
	public HttpServletRequest getReq() {
		return req;
	}
	public HttpServletResponse getRes() {
		return res;
	}
	private HttpServletResponse res;
	

	
	static final String OP_="OP_";
	//public static final String headAttr="_head";
	public static final String ATTR_CONTENT="content"; //"[[110414_052728@"+Origin.uid+"]]";
	public static final String ATTR_BODY = "body";
	public static final String AJAXTAG = "AJAXTAG:";
	public static final String ATTR_ARGUMENTORDER="argumentOrder";

	
	
	Map<String,String> _params=null;
	static final String ATTR_FORMAT = "_format";
	static final String ATTR_PRECONTENT = "precontent";
	public static final String ATTR_SCOPE = "scope";
	private static final String DOGET = "doGet";
	
    public Map<String,String> params() {
		if (_params!=null) return _params;
    	Map<String,Object> m=req.getParameterMap();
		Map<String,String> res=new Hashtable<String, String>();
		for (String k:m.keySet()) {
			Object vo=m.get(k);
			if (vo instanceof String[]) {
				String[] str = (String[]) vo;
				String val=Util.toSingleton(str);
				res.put(k, val);
			}
		}
		_params=res;
		return res;
	}

    public Map<String,Object> params(final Map<String, ?> typeHints) {
    	final Map<String, String> p = params();
    	final Map<String,Object> res=new HashMap<String, Object>();
    	Maps.entries(p).each(new MapAction<String, String>() {
			
			@Override
			public void run(String key, String value) {
				String type = typeHints.get(key)+"";
				Object o;
				if (value==null) {
					o=null;
				} else	if (type.startsWith("?doc")) {
					Matcher m = DocumentProcessor.idpatWiki.matcher(value);
					String id;
					if (m.lookingAt()) {
						id=m.group(1);
					} else id=value;
					o=documentLoader.byId(id);									
				} else 	if (type.startsWith("?str")) {
					o=value;
				} else {
					Matcher m = DocumentProcessor.idpatWiki.matcher(value);
					String id;
					if (m.lookingAt()) {
						id=m.group(1);
						o=documentLoader.byId(id);
					} else {
						m = Literal.DQ.matcher(value);
						if (m.lookingAt()) {
							o=Literal.fromQuoteStrippedLiteral(m.group(1));
						} else {
							o=value;
						}
					}
				}
				Log.d("Param", key+"="+key+" o="+o+" src="+value+" type="+type);
				res.put(key,o);
			}
		});
    	return res;
    }
	public String[] args() {
    	String str=req.getPathInfo();
        String[] s=str.split("/");
        return s;
    }
	public String[] execArgs() {
		//0  1    2   3     
		// /exec/id/args0/args1
		int start=3;
		String[] src = args();
		String[] res=new String[src.length-start];
		System.arraycopy(src,start,  res,0, res.length);
		return res;
		                        
	}
    public String queryString() {
        String query = req.getQueryString();
        return (query==null?"":query);
    }
    public DocumentProcessor documentProcessor(DocumentScriptable d) {return new DocumentProcessor(d, this);}
    public void proc() throws IOException {
    	final Ref<IOException> ee=new Ref<IOException>();
    	cur.enter(this, new Runnable() {
			@Override
			public void run() {
				try {
					proc2();
				}catch (Exception e) {
					//ee.set(e);
					try {
						Log.d(this, "spawned Error - "+e);
						res.setContentType(TEXT_PLAIN_CHARSET_UTF_8);
						Httpd.respondByString(getRes(), "Error - "+e);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				}
			}
		});
    	if (ee.isSet()) throw ee.get();
	}	
    private void proc2() throws IOException
    {
		req.setCharacterEncoding("UTF-8");
		res.setContentType(TEXT_HTML_CHARSET_UTF_8);
		String[] s=args();
        Log.d(this,"pathinfo = "+req.getPathInfo());
        Log.d(this,"qstr = "+req.getQueryString());
        if (s.length>=2 && (s[1].equalsIgnoreCase("byid") || s[1].equalsIgnoreCase("view")) ) {
            view();
        }
        else if (s.length >= 3 && s[1].equalsIgnoreCase("exec")) {
        	exec();
        }
        else if (s.length == 2 && s[1].equalsIgnoreCase("auth")) {
        	auth();
        } 
        else if (s.length == 2 && s[1].equalsIgnoreCase("new")) {
        	newDocument();        	
        }
        else if (s.length >= 3 && s[1].equalsIgnoreCase("edit")) {
        	edit();
        }
        else if (s.length >= 3 && s[1].equalsIgnoreCase("customedit")) {
        	customEdit();
        }
        else if (s.length >= 3 && s[1].equalsIgnoreCase("editbody")) {
        	editBody();
        }
        else if (s.length==2 && s[1].equalsIgnoreCase("all")) {
        	all();
        }
        else if (s.length>=2 && s[1].equalsIgnoreCase("upload")) {
        	upload();
        }
        else if (s.length>=2 && s[1].equalsIgnoreCase("fileupload")) {
        	fileUpload();
        }
        else if (s.length>=2 && s[1].equalsIgnoreCase("fileuploaddone")) {
        	fileUploadDone();
        }
        else if (s.length>=2 && s[1].equalsIgnoreCase("download")) {
        	download();
        }
        else if (s.length>=2 && s[1].equalsIgnoreCase("search")) {
        	search();
        }
        else if (s.length>=2 && s[1].equalsIgnoreCase("sendsync")) {
        	sendSync();
        }
        else if (s.length>=2 && s[1].equalsIgnoreCase("recvsync")) {
        	recvSync();
        }
        else if (s.length>=2 && s[1].equalsIgnoreCase("browserjs")) {
        	browserjs();
        }
        else if (s.length>=2 && s[1].equals("import1")) {
        	importFromVer1();
        }
        else if (s.length>=2 && s[1].equals("errorlog")) {
        	errorLog();
        }
        else if (req.getPathInfo().equals("/")) {
        	if (fullURL().endsWith("/")) {
        		topPage();
        	} else {
        		redirect(fullURL()+"/");
        	}
        }
        else {
        	byName();
        }
    }
    private void errorLog() throws IOException {
    	res.setContentType(TEXT_PLAIN_CHARSET_UTF_8);
    	PrintWriter w=res.getWriter();
    	w.println(Log.errorLog.getBuffer());
    	w.close();
	}
	public String browserjsPath(Class klass) {
    	return rootPath()+"/browserjs/"+klass.getName();
    }
    private void browserjs() throws IOException {
		//0  1          2        
		// /browserjs/path.to.Class
    	String[] a = args();
    	if (a.length<3) return ;
    	try {
			Class c = Class.forName(a[2]);
			String src = Resource.text(c, ".js");
			res.setContentType("text/javascript");
			Httpd.respondByString(res,src);
    	} catch (ClassNotFoundException e) {
			notfound("Class "+a[2]+" Not found.");
		}    	
	}
	private void fileUpload() {
    	//new FileUpload().uploadForm(this);		
	}
    private void fileUploadDone() {
    	//new FileUpload().uploadDone(this);		
	}
	private DocumentScriptable getSyncProf(String name) {
		String syncProfId=params().get(name);
    	DocumentScriptable syncProf=documentLoader.byId(syncProfId);
		return syncProf;
	}
	static final String LOCAL_LAST_SYNCED= "localLastSynced";
	static final String REMOTE_LAST_SYNCED= "remoteLastSynced";
	private static final String LOCAL_SYNCID = "localsyncid";
	private static final String REMOTE_SYNCID = "remotesyncid";
	private static final Object SETLASTUPDATE = "SetLastUpdate";
	private long getLongProp(Scriptable s, String name) {
		Object o=ScriptableObject.getProperty(s,name);
		if (o instanceof Number) {
			Number l = (Number) o;
			return l.longValue();
		}
		return -1;
	}
    /* input param:
     *   localsyncid=(this system's sync profile id)
     *   credential=
     *   data= [DocumentRecord] ...  
     *   downloadsince= (optional)
     * output:
     *   [DocumentRecord]...   (since this.syncProf.localLastSynced)
     *   [SetLastUpdate]
     *   generated log id(after saved all inputs!)
     * changes:
     *   this.syncProf.localLastSynced    -> generated log id
     *   this.syncProf.remoteLastSynced   -> max (input DocumentRecord.lastUpdate)
    */
    private void recvSync() throws IOException {
    	DocumentScriptable localSyncProf = getSyncProf(LOCAL_SYNCID);
    	
    	// download
    	Object sinceo=ScriptableObject.getProperty(localSyncProf, LOCAL_LAST_SYNCED);
		String sinces=params().get(DOWNLOADSINCE);
		final long since;
		if (sinces!=null) {
			since=Long.parseLong(sinces);
		} else if (sinceo instanceof Number) {
			Number n = (Number) sinceo;
			since=n.longValue();
		} else {
			since=-1;
		}
    	res.setContentType (TEXT_PLAIN_CHARSET_UTF_8);
    	final PrintWriter writer=res.getWriter();
    	exportDocuments(since, writer,null);   	 	
    	// upload
        String data=params().get(DATA);
		StringReader rd=new StringReader(data);
		Scanner sc=new Scanner(rd);
		long newRemoteLastSynced=importDocuments(sc,null,null);
		
		// update local last synced
		long newLocalLastSynced=documentLoader.getDocumentSet().log(
				new Date()+"", "sync", params().get(LOCAL_SYNCID), "");
		ScriptableObject.putProperty(localSyncProf,LOCAL_LAST_SYNCED,newLocalLastSynced);
		ScriptableObject.putProperty(localSyncProf,REMOTE_LAST_SYNCED,newRemoteLastSynced);
        writer.println("["+SETLASTUPDATE+"]");
        writer.println(newLocalLastSynced);

		localSyncProf.save();
		
    }
    /* input param:
     *   remotesyncid=(remote's sync profile id)
     *   localsyncid=(this system's sync profile id)
     * Connect to this.syncProf.url with:
     *   data= [DocumentRecord] ...  (since this.syncProf.localLastSynced)
     *   localsync=remotesyncid
     *   downloadsince = (this.syncProf.remoteLastSynced)
     * Receives
     *   [DocumentRecord]...
     *   [SetLastUpdate]
     *   remote's generated log id
     * changes:
     *   this.syncProf.localLastSynced  -> generated log id
     *   this.syncProf.remoteLastSynced -> max (received DocumentRecord.lastUpdate)
     */
    private void sendSync() throws IOException {
		res.setContentType(TEXT_HTML_CHARSET_UTF_8);
		PrintWriter w=res.getWriter();

		DocumentScriptable localSyncProf = getSyncProf(LOCAL_SYNCID);
		w.println("LocalSyncProfile = "+localSyncProf+"<BR>");
		
		
    	String urls=""+ScriptableObject.getProperty(localSyncProf, "url");
    	StringWriter data=new StringWriter();
    	PrintWriter pdata=new PrintWriter(data);
    	long uploadsince=getLongProp(localSyncProf, LOCAL_LAST_SYNCED);
    	Vector<String> exported=new Vector<String>(), imported=new Vector<String>();
    	exportDocuments(uploadsince, pdata,exported);
    	String remoteSyncid=params().get(REMOTE_SYNCID);
		w.println("RemoteSyncProfile = "+remoteSyncid+"<BR>");
    	long downloadsince=getLongProp(localSyncProf, REMOTE_LAST_SYNCED);

		w.println("uploadsince= "+uploadsince+"<BR>");
		w.println("downlocalsince= "+downloadsince+"<BR>");

    	
    	String recv=HttpPost.send(urls, Maps.create(DATA, data.getBuffer()+"")
    			.p(LOCAL_SYNCID,remoteSyncid).p(DOWNLOADSINCE,""+downloadsince));
    	Scanner sc=new Scanner(new StringReader(recv));
    	
    	Set<String> excludes=new HashSet<String>();
    	excludes.addAll(exported);
		long newRemoteLastSynced=importDocuments(sc,imported,excludes);
		
		// update local last synced
		long newLocalLastSynced=documentLoader.getDocumentSet().log
				(new Date()+"", "sync", params().get(LOCAL_SYNCID), "");
		ScriptableObject.putProperty(localSyncProf,LOCAL_LAST_SYNCED,newLocalLastSynced);
		ScriptableObject.putProperty(localSyncProf,REMOTE_LAST_SYNCED,newRemoteLastSynced);
		localSyncProf.save();
		
		w.println("<HR>Exported : <BR>");
		for (String s:exported) {
			w.println(s+" ");
		}
		w.println("<HR>Imported : <BR>");
		for (String s:imported) {
			w.println(s+" ");
		}
		w.println("<HR>new LocalLastSynced = "+newLocalLastSynced+"<BR>");
		w.println("new RemoteLastSynced = "+newRemoteLastSynced+"<BR>");
    }
	private void download() throws IOException {
		//input param:
		//  dbid=(client's DBID)
		//  credential=(client's user name or something)
		//  since=(= client's remoteLastsync) displays DocumentRecord.lastupdate>since 
		//output:
		//  [DocumentRecord]
		//  [DocumentRecord]
		//  :
		//note: client's localLastsync set to client's new log id
		//note: client's remoteLastsync set to max(DocumentRecord.lastUpdate)

		final StringBuffer buf = new StringBuffer();
		String sinces=params().get("since");
		final long since;
		if (sinces!=null) {
			since=Long.parseLong(sinces);
		} else {
			since=-1;
		}
    	res.setContentType (TEXT_PLAIN_CHARSET_UTF_8);
    	final PrintWriter writer=res.getWriter();
        exportDocuments(since, writer, null);   	
      
	}
	private void exportDocuments(final long since, final PrintWriter writer,
			final List<String> exportedIds) {
		documentLoader.search("", null, new BuiltinFunc() {		
			@Override
			public Object call(Context cx, Scriptable scope, Scriptable thisObj,
					Object[] args) {
				DocumentScriptable s=(DocumentScriptable)args[0];
				DocumentRecord document = s.getDocument();
				Log.d("CompLastup", document.lastUpdate+" - "+since);
				if (document.lastUpdate>since) {
					document.export(writer);
					if (exportedIds!=null) exportedIds.add(document.id);
					return false;
				} else return true;
			}
		});
	}
	private void upload() throws IOException {
		//input params:
		//  syncid=(this server's syncProfile id)
		//  credential=(client's user name or something)
		//  data=
		//    [DocumentRecord]
		//    [DocumentRecord]
		//    :
		//    (contain since client's localLastSynced)
		//output:
		//  none
		//note: This system's remoteLastsync set to max(DocumentRecord.lastupdate)
		//note: This system's localLastsync set to client's new log id
		
		if ("get".equalsIgnoreCase( req.getMethod())){
			print(Html.p("<html><body>" +
					"<form action=%a method=POST>"+
					"syncid=<input name=syncid><BR>"+
					"credential=<input name=credential><BR>"+
					"data:<BR><textarea name=data rows=40 cols=80></textarea><BR>"+
					"<input type=submit>"+
					"</form>" +
					"</body></html>",
					rootPath()+"/upload"));
		} else {
			String data=params().get(DATA);
			StringReader rd=new StringReader(data);
			Scanner sc=new Scanner(rd);
			importDocuments(sc,null,null);
		}
	}
	private long importDocuments(Scanner sc, List<String> importedIds, Set<String> excludes) {
		long newRemoteLastSynced=0;
		try {
			List<DocumentRecord> loaded=new Vector<DocumentRecord>();
			String nextCl=null;
			while (true) {
				DocumentRecord d = new DocumentRecord();
				nextCl=d.importRecord(sc);
				if (!d.tableName().equals(nextCl) ) break;
				if (d.content!=null) {
					if (d.lastUpdate>newRemoteLastSynced) newRemoteLastSynced=d.lastUpdate;
					if (excludes==null || !excludes.contains(d.id)) {
						if (importedIds!=null) importedIds.add(d.id);
						loaded.add(0,d);
					}
				}
			}
			documentLoader.importDocuments(loaded);
			if (SETLASTUPDATE.equals(nextCl)) {
				newRemoteLastSynced=sc.nextLong();
			}
		} catch (SqlJetException e) {
			e.printStackTrace();
		}
		return newRemoteLastSynced;
	}
	private void topPage() throws IOException {
		/*Cursor s=query(Query.create("topPage:=true"));
		Document d=null;
		while (s.hasNext()) {
			d=s.next().document();
			documentProcessor(  d).execHtml();
			break;
		}
		s.close();
		if (d==null) all();*/
		final Ref<Boolean> execed = Ref.create(false);
		documentLoader.searchByQuery(Query.create("topPage:true"),new BuiltinFunc( ) {
			
			@Override
			public Object call(Context cx, Scriptable scope, Scriptable thisObj,
					Object[] args) {
				exec((DocumentScriptable)args[0]);
				execed.set(true);
				return true;
			}
		});
		if (!execed.get()){
			all();
		}
	}
	private void view() throws IOException {
        String[] s=args();
		String id = s[2];
		DocumentScriptable d = (DocumentScriptable)documentLoader.byId(id);
		if (d != null)
		{
		    documentProcessor(d).proc();           
		}
		else
		{
		    notfound(id);
		}
	}
	private void exec()
			throws IOException {
		//Map<String,String> params=params();
        String[] s=args();
        
        
		String id=s[2];
		final DocumentScriptable d= documentLoader.byId(id);
        
		if (d!=null) {
	        /*CompileResult o=JSSession.cur.get().compile(d);
	        boolean execed=false;
	        if (o instanceof SWebApplication) {
				SWebApplication app = (SWebApplication) o;
				app.run();
				execed=true;
			}*/
			/*
	        if (o!=null) {
	        	SWebApplication app=o.value(SWebApplication.class);
	        	if (app!=null) {
	        		app.run();
	        		execed=true;
	        	}
			}*/
			boolean execed = exec(d);
	        if (!execed) {
	        	print(id+" is not executable :"+d);
	        }
		} else {
			 notfound(id);
		}
	}
	private boolean exec(final DocumentScriptable d) {
		return exec(d,d);
	}
	private boolean exec(final DocumentScriptable d, final Scriptable thiz) {
		boolean execed=false;
		Object doGet = ScriptableObject.getProperty(d, DOGET);
		if (doGet instanceof Function ) {
			final Function f=(Function) doGet;
			JSSession.withContext(new ContextRunnable() {
				
				@Override
				public Object run(Context cx) {
					if (Args.getArgs(f).length>1) {
						f.call(cx, jssession().root, thiz, 
							new Object[]{getReq(),getRes(),HttpContext.this});
					} else {
						f.call(cx, jssession().root, thiz, 
								new Object[]{HttpContext.this});
					}
					return null;
				}
			});
			execed=true;
		}
		return execed;
	}
	private JSSession jssession() {
		return JSSession.cur.get();
	}
	/*public Cursor query(Query q) {
		Debug.syslog("Query starting "+q.toString());
		Cursor cursor = documentSet().query(q);
		Debug.syslog("Query started "+q.toString());
		return cursor;
	}*/
	private void byName() throws IOException {
		String name=req.getPathInfo().replaceAll("^/", "").replaceAll("/.*", "");
		Query q=QueryBuilder.create("name:?").tmpl("name", name, AttrOperator.exact).toQuery();
		final Ref<Boolean> found=Ref.create(false);
		documentLoader.searchByQuery(q, new BuiltinFunc() {
			@Override
			public Object call(Context cx, Scriptable scope, Scriptable thisObj,
					Object[] args) {
				DocumentScriptable s=(DocumentScriptable)args[0];
				try {
					if (!exec(s)) {
						documentProcessor(s).proc();
					}
					found.set(true);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
			}
		});
		if (!found.get()) {
			notfound(name);
		}
		
	}
	private String contentStatus(ContentChecker c) {
		StringBuilder msg=new StringBuilder(c.getMsg()+"<br/>\n");
		
		
		for (String name:c.getUndefinedSymbols()) {
			String sel = SEL+name;
			String searchAddr = Html.p(rootPath()+"/search?sel=%u&q=%u",sel, "name:"+name);
			msg.append(Html.p("<a href=%a target=%a>%t</a> <input id=%a name=%a/> <br/>\n", 
					 searchAddr,
					 "frame_"+name,
					 name,
					 sel,
					 sel
			));
		}
		return msg+"";
	}
	private void newDocument() throws IOException {
		String content="$.extend(_,{\n    name:\"New_Document\"\n});";
		String ctr=params().get("constructor");
		if (ctr!=null) {			
			content=sprintf(
					"$.extend(_,{\n"+
					"  name:\"New_Document\",\n"+
			        "  constructor: $.byId(%s) \n" +
			        "});"
					, toLiteral(ctr) ); ;
		}
		String msg="";
		if (req.getMethod().equals("POST")) {
			content=params().get(ATTR_CONTENT);
			String[] reqs = getRequires();
			ContentChecker c=new ContentChecker(content,addedVars(),reqs);
			if (c.check()) {
				DocumentScriptable d = documentLoader.newDocument(null);
				documentProcessor(d).proc();
				return;
			}
			content=c.getChangedContent();
			msg=contentStatus(c);
		}
		Httpd.respondByString(res, Html.p("<html><title>New Document</title>"+
				"<body><form action=%a method=POST>%s"+
				"<!--preContent: <br/>\n"+
				"<input name=%a /><br/-->\n"+
				"Requires: <input name=requires><BR>"+
				"Content: <br/>\n"+
				"<textarea id=edit name=%a rows=25 cols=60>%t</textarea>"+
				"<input type=submit>"+
				"</form>"+indentAdap()+"</body></html>",
				 "./new", msg, ATTR_PRECONTENT, ATTR_CONTENT , content)
		);
	}
	private String[] getRequires() {
		String[] reqs=new String[0];
		String reqss=params().get("requires");
		if (reqss!=null && reqss.trim().length()>0) reqs=reqss.split("\\W+");
		return reqs;
	}
	private String indentAdap() {
		return  Html.p("<script src=%a></script><script>attachIndentAdaptor('edit')</script>"
				,browserjsPath(IndentAdaptor.class));
	}
	DocumentScriptable target;
	public Scriptable targetDocument() {
		return target;
	}
	private boolean customEdit() throws IOException {
		String[] s=args();
		//   $soyText/customedit/00000 
		//   $soyText/customedit/00000?defaultEditor=id
		String id=s[2];
		String msg="";
		target = documentLoader.byId(id);
		if (target==null) {
		    notfound(id);
		    return false;
		}	
		String defEdit= params().get("defaultEditor");
		boolean execed=false;
		Object doEdit=ScriptableObject.getProperty(target, DO_EDIT);
		if (doEdit instanceof Function ) {
			final Function f=(Function) doEdit;
			JSSession.withContext(new ContextRunnable() {
				
				@Override
				public Object run(Context cx) {
					if (Args.getArgs(f).length>1) {
						f.call(cx, jssession().root, target, 
							new Object[]{getReq(),getRes(),HttpContext.this});
					} else {
						f.call(cx, jssession().root, target, 
								new Object[]{HttpContext.this});
					}
					/*	
					f.call(cx, jssession().root, target, 
							new Object[]{getReq(),getRes(),HttpContext.this});
					*/
					return null;
				}
			});
			execed=true;
		} else if (defEdit!=null){
			//DocumentScriptable defEditDoc = documentLoader.byId(defEdit);
			redirect(rootPath()+"/exec/"+defEdit+"?doc="+target);
		} else {
			edit();
		}
		return execed;
	}
	private void edit() throws IOException {
		String[] s=args();
		//   $soyText/edit/00000
		String id=s[2];
		String msg="";
		target = documentLoader.byId(id);
		if (target==null) {
		    notfound(id);
		    return;
		}
		String content = target.getDocument().content;	
		if (req.getMethod().equals("POST")) {
			content=params().get(ATTR_CONTENT);
			String[] reqs = getRequires();
			ContentChecker c=new ContentChecker(content,addedVars(),reqs);
			if (c.check()) {
				documentProcessor(target).proc();
				return;
			}
			content=c.getChangedContent();
			msg=contentStatus(c);
		}
		String preContent = target.getDocument().preContent;
		Httpd.respondByString(res, menuBar()+Html.p(
				"<form action=%a method=POST>%s"+
				"<!--preContent: <br/>\n"+
				"<input name=%a value=%a /><br/-->"+
				"Requires: <input name=requires><BR>"+
				"Content: <br/>\n"+
				"<textarea id=edit name=%a rows=20 cols=80>%t</textarea>"+
				"<input type=submit>"+
				"</form>"+indentAdap()+"</body></html>",
				"./"+id, 
				msg,
				HttpContext.ATTR_PRECONTENT,
				preContent==null?"":preContent,
						HttpContext.ATTR_CONTENT, content)
		);

	}

	private Map<String,String> addedVars() {
		final Map<String,String> b=new HashMap<String, String>();
		Maps.entries(params()).each(new MapAction<String, String>() {
			@Override
			public void run(String key, String value) {
				if (key.startsWith(SEL) && value.length()>0) {
					String name=key.substring(SEL.length());
					b.put(name,value);//(SPrintf.sprintf("var %s=%s;\n", name, value));
				}
			}
		});
		return b;
	}
	private void editBody() throws IOException {
		String[] s=args();
		//   $soyText/edit/00000
		String id=s[2];
		DocumentScriptable d = documentLoader.byId(id);
		if (d==null) {
		    notfound(id);
		} else if (req.getMethod().equals("POST")) {
			documentProcessor(d).proc();
		} else {
			
			Httpd.respondByString(res, menuBar()+Html.p(
					"<form action=%a method=\"POST\">"+
					"Body: <br/>\n"+
					"<textarea id=edit name=%a rows=20 cols=80>%t</textarea>"+
					"<input type=submit>"+
					"</form>"+indentAdap()+"</body></html>",
					"./"+id, HttpContext.ATTR_BODY, d.get(ATTR_BODY)+"")
			);
		}
	}
    private void auth() throws IOException {
		String user=params().get("user");
		String pass=params().get("pass");
		Session s=null;
		String msg="";
		if ("logout".equals(user) || (user!=null && user.length()>0 && pass!=null && pass.length()>0)) {
			if ("logout".equals(user)) {
				user="";
				s=Session.NOBODY;
			}  else {
				Authenticator a=documentLoader.authenticator();
				if (a!=null && a/*AuthentificatorList.alist*/.check(user, pass)) {
					s=SessionSet.create(user);
		    		res.addCookie(new Cookie(SESSION_NAME, s.id()));
		    		String after=params().get("after");
		    		if (after!=null) {
		    			res.sendRedirect(rootPath()+"/"+after);
		    		} else {
		    			res.sendRedirect(rootPath()+"/");
		    		}
				} else {
					msg="ユーザ名、パスワードが間違っています。";
				}
			}
    	} 
		if (s==null) {
    		if (user==null) user="";
    		String aft="";
    		String after=params().get("after");
		    if (after!=null) aft=Html.p("<input type=hidden name=after value=%a/>",after);
			Httpd.respondByString(res, msg+"<form action=\"./auth\" method=\"POST\">"+
    				"ユーザ名： <input name=\"user\" value=\""+user+"\"><br/>"+
    				"パスワード: <input type=\"password\" name=\"pass\">"+
    				aft+
    				"<br><input type=submit>"+
    				"</form>"
    		);
    	}
	}
    public String fullURL() {
    	return req.getRequestURL()+"";
    }
    public String absoluteRootPath() {
    	String res=fullURL();
    	int length=args().length;
    	//  docBase()/byId/****
   		// http://host/soytext2     args=[""]
   		// http://host/soytext2/     args=["",""]
   		// http://host/soytext2/aaa     args=["","aaa"]
    	// http://host/soytext2/aaa/bbb   args=["","aaa","bbb"]
    	// http://host/soytext2/aaa/bbb/   args=["","aaa","bbb",""]
    	while (length>=2) {
    		res=res.replaceAll("/[^/]*$", "");
   			length--;
   		}
		return res;
    }
    public String encodeURI(String str) throws UnsupportedEncodingException {
    	return URLEncoder.encode(str, "utf-8");
    }
    public String encodeHTML(String str) {
    	return HTMLDecoder.encode(str);
    }
    public void redirect(String url) {
    	try {
    		res.sendRedirect(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	private void all() throws IOException {
		search("",null);
	}
	public String linkBar(DocumentScriptable ds) {
		return linkBar(ds,null);
	}
	/**
	 * 
	 * @param ds
	 * @param sel
	 * @return
	 */
	public String linkBar(DocumentScriptable ds,String sel) {
		DocumentRecord d=ds.getDocument();
		String id=d.id; 
		
		if (isAjaxRequest()) {
			return 	Util.encodeTabJoin(new Object[] {d.lastUpdate , id, d.summary})+"\n";
		} else {
			String selt="";
			if (sel!=null) {
				selt=Html.p("<a href=%a>Sel</a> ", 
					  SPrintf.sprintf(
						 "javascript:window.opener.document.getElementById(%s).value=%s;window.close();",
						 Literal.toLiteral(sel), 
						 Literal.toLiteral(SPrintf.sprintf("$.byId(%s)",Literal.toLiteral(id)))
					  )
				);
			}
			return Html.p(
				"<!--%t-->"+
				"%s <a href=%a>View</a>  "+
				"<a href=%a>Edit</a> "+
				"<a href=%a>EditBody</a> "+
				"<a href=%a>CustomEdit</a> "+
				"<a href=%a>Exec</a> "+
				"<a href=%a>NewObj</a> "+
				"%t<br/>\n"
				, AJAXTAG+id
				, selt
				, rootPath()+"/view/"+id 
				, rootPath()+"/edit/"+id 
				, rootPath()+"/editbody/"+id 
				, rootPath()+"/customedit/"+id 
				, rootPath()+"/exec/"+id 
				, rootPath()+"/new?constructor="+id 
				, d.summary);
		}
	}
	private boolean isAjaxRequest() {
		return "ajax".equals( params().get("_responseType") );
	}
	private void search() throws IOException
    {
    	Map<String,String> params=params();
    	//args[2]: id of savedsearch
		String docBase=rootPath();
        String cstr=params.get("q");
        Log.d(this,"cstr = "+cstr);
    	if (cstr==null) {
    		Httpd.respondByString(res,"<form action=\"search\" method=POST><input name=q></form>");
    	} else {
    		search(cstr,params.get("sel"));
    		
    	}
    }
    private void search(String cstr, final String sel) throws IOException {
    	if (assertRoot()) return;
    	final StringBuffer buf = new StringBuffer(isAjaxRequest() ? "" : menuBar());
        documentLoader.search(cstr, null, new BuiltinFunc() {		
        	int c=0;
			@Override
			public Object call(Context cx, Scriptable scope, Scriptable thisObj,
					Object[] args) {
				DocumentScriptable s=(DocumentScriptable)args[0];
				buf.append(linkBar(s,sel));
				c++;
				return c>100;
			}
		});   	
        buf.append("<BR>insts= "+SMain.insts);
    	res.setContentType (TEXT_HTML_CHARSET_UTF_8);
        Httpd.respondByString(res, buf.toString());
	}
	public String rootPath() {
    	int length=args().length;
		//  docBase()/byId/****
		if (length<=2) {
			// $SOYTEXT/aaa     args=["","aaa"]
			return ".";
		}
		StringBuffer buf=new StringBuffer();
		String cmd="";
		while (length>2) {
			buf.append(cmd+"..");
			cmd="/";
			length--;
		}
		return buf.toString();
	}
    public void notfound( String searchString)
    throws IOException {
    	res.setStatus(404);
    	Httpd.respondByString(res,searchString+" Not found");
    }
    public void frobidden( String searchString)
    throws IOException {
    	res.setStatus(403);
    	Httpd.respondByString(res,searchString);
    }

	String menuBar() {
		String q=params().get("q");
		if (q==null) q="";
		String path=rootPath();
		StringBuilder buf=new StringBuilder();
        buf.append(Html.p("<html><head><meta http-equiv=%a content=%a></head>"
        		                ,CONTENT_TYPE,TEXT_HTML_CHARSET_UTF_8));
        buf.append("<body>");
        buf.append("User: "+currentSession().userName()+" | ");
        buf.append(Html.p("<a href=%a>ログイン</a>  |" , path+"/auth"));
        buf.append(Html.p("<a href=%a>ホーム</a>  |" , path+"/all"));
        buf.append(Html.p("<a href=%a>新規作成</a> | ", path+"/new"));
        buf.append(Html.p("<form action=%s method=POST style=\"display: inline;\">" +
        		"<input name=q value=%s></form>", path+"/search" ,q));
        //buf.append(Html.p("<a href=%a>検索</a> |\n" , path+"/search"));
        buf.append("DB: "+documentSet());
        buf.append(Html.p("Err: <a href=%a>%s</a>", 
        		path+"/errorlog", ""+Log.errorLog.getBuffer().length()));
        buf.append("<HR>");
        return buf.toString();
	}
	
	/*public static String lastModifiedField(Document d)
	{
	    return TimeFormat.toRFC2822(d.lastUpdate());//TimeFormat.toUtcTicks(d.lastUpdate(), TimeZone.getDefault())); 
	}*/
	public static String detectContentType(String fileName) {
		return detectContentType(fileName, TEXT_PLAIN_CHARSET_UTF_8);
	}
	public static String detectContentType(String fileName,String def) {
	    if (fileName != null)
	    {
	    	fileName=fileName.toLowerCase();
	        if (fileName.endsWith(".js"))
	        {
	            def = "text/javascript; charset=utf-8";
	        }
	        if (fileName.endsWith(".css"))
	        {
	            def = "text/css; charset=utf-8";
	        }
	        if (fileName.endsWith(".html"))
	        {
	            def = TEXT_HTML_CHARSET_UTF_8;
	        }
	        if (fileName.endsWith(".gif"))
	        {
	            def = "image/gif";
	        }
	        if (fileName.endsWith(".ico"))
	        {
	            def = "image/vnd.microsoft.icon";
	        }
	        if (fileName.endsWith(".jpg"))
	        {
	            def = "image/jpeg";
	        }
	        /*if (fileName.endsWith(".jar"))
	        {
	            def = "application/x-java-applet";
	        }*/
	    }
	    return def;		
	}
	
	public static String detectContentType(DocumentScriptable d)
	{
	    Object c = d.get(CONTENT_TYPE);
	    if (c instanceof String) return c.toString();
	    c = d.get("Content-type");
	    if (c instanceof String) return c.toString();
	    String n = d.get("name")+"";
	    Log.d("HTPCON", "Detecting "+d.getDocument().id+" - "+n);
	    return detectContentType(n);
	  /*  c = "text/plain; charset=utf-8";
	    if (n != null)
	    {
	    	n=n.toLowerCase();
	        if (n.endsWith(".js"))
	        {
	            c = "text/javascript; charset=utf-8";
	        }
	        if (n.endsWith(".css"))
	        {
	            c = "text/css; charset=utf-8";
	        }
	        if (n.endsWith(".html"))
	        {
	            c = "text/html; charset=utf-8";
	        }
	        if (n.endsWith(".gif"))
	        {
	            c = "image/gif";
	        }
	        if (n.endsWith(".png"))
	        {
	            c = "image/png";
	        }
	        if (n.endsWith(".jpg"))
	        {
	            c = "image/jpeg";
	        }
	    }
	    return c.toString();*/
	}
	/*public Compiler evaluator() {
		return currentSession().evaluator();
	}*/
	/*
	public Doc2JS doc2js() {
		return currentSession().doc2js();
	}
	public Doc2Dtl doc2dtl() {
		return currentSession().doc2dtl();
	}*/
	public static String ajaxTag(String string) {
		return "<!--"+AJAXTAG+string+"-->";
	}
	/*public FileDocumentSet fileDocumentSet() {
		if (documentSet() instanceof FileDocumentSet) {
			FileDocumentSet f = (FileDocumentSet) documentSet();
			return f;
		}
		if (documentSet() instanceof MultiDocumentSet) {
			MultiDocumentSet m = (MultiDocumentSet) documentSet();
			if (m.primaryDocumentSet() instanceof FileDocumentSet) {
				FileDocumentSet f = (FileDocumentSet) m.primaryDocumentSet();
				return f;
			}
		}
		return null;
	}*/
	public void importFromVer1() {
		try {
			URL u=new URL("http://localhost:3001/exec/110412_045800?after=1307074166184");
			InputStream in=(InputStream)u.getContent();
			File file = new File("import/import.txt");
			Scanner sc=new Scanner(in);
			PrintWriter w=new PrintWriter(file);
			w.println("[Document]");
			while (sc.hasNextLine()) {
				String l=sc.nextLine();
				if (l.startsWith("<pre>") || l.startsWith("</pre>")) continue;
				w.println(l);
				//System.out.println(l);
			}
			sc.close();
			w.close();
			
			Importer i=new Importer(documentLoader);
			i.importDocuments(file);
			
			all();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
