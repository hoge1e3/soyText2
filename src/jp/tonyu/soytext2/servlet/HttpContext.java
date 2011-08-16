package jp.tonyu.soytext2.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.tonyu.debug.Log;
import jp.tonyu.js.BuiltinFunc;
import jp.tonyu.js.ContextRunnable;
import jp.tonyu.soytext.Origin;
import jp.tonyu.soytext2.browserjs.IndentAdaptor;
import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.document.DocumentAction;
import jp.tonyu.soytext2.document.DocumentSet;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.document.backup.Importer;
import jp.tonyu.soytext2.js.CompileResult;
import jp.tonyu.soytext2.js.CompilerResolver;
import jp.tonyu.soytext2.js.ContentChecker;
import jp.tonyu.soytext2.js.DefaultCompiler;
import jp.tonyu.soytext2.js.DocumentLoader;
import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.js.JSSession;
import jp.tonyu.soytext2.search.Query;
import jp.tonyu.soytext2.search.QueryBuilder;
import jp.tonyu.soytext2.search.expr.AttrOperator;
import jp.tonyu.soytext2.value.Value;
import jp.tonyu.soytext2.value.Values;
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
import static jp.tonyu.util.SPrintf.sprintf;
import static jp.tonyu.util.Literal.toLiteral;

public class HttpContext {
	private static final String SEL = "sel_";
	public static final jp.tonyu.util.Context<HttpContext> cur=new jp.tonyu.util.Context<HttpContext>();
	private static final String SESSION_NAME = "soyText_Session";
	/*public final soytext.script.Context context= new soytext.script.Context(true);
	public SessionManager sessionManager() {
		return appCtx.sessionManager;
	}
	Session currentSession=null;
	public Session currentSession() {
		if (currentSession!=null) return currentSession;
		if (req.getCookies()!=null) {
			for (Cookie c:req.getCookies()) {
				if (c.getName().equals(SESSION_NAME)) {
					return currentSession= sessionManager().get(c.getValue());
				}
			}
		}
		return currentSession=sessionManager().defaultSession();
	}
	public ApplicationContext applicationContext() {
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
						Httpd.respondByString(getRes(), "Error - "+e.getMessage());
					} catch (IOException e1) {
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
		res.setContentType("text/html; charset=utf8");
		String[] s=args();
        Log.d(this,"pathinfo = "+req.getPathInfo());
        Log.d(this,"qstr = "+req.getQueryString());
        if (s.length>=2 && s[1].toLowerCase().equals("byid") ) {
            byId();
        }
        else if (s.length >= 3 && s[1].toLowerCase().equals("exec")) {
        	exec();
        }
        else if (s.length == 2 && s[1].toLowerCase().equals("auth")) {
        	auth();
        } 
        else if (s.length == 2 && s[1].toLowerCase().equals("new")) {
        	newDocument();        	
        }
        else if (s.length >= 3 && s[1].toLowerCase().equals("edit")) {
        	edit();
        }
        else if (s.length >= 3 && s[1].toLowerCase().equals("editbody")) {
        	editBody();
        }
        else if (s.length==2 && s[1].equals("all")) {
        	all();
        }
        else if (s.length>=2 && s[1].equals("upload")) {
        	upload();
        }
        else if (s.length>=2 && s[1].equals("fileupload")) {
        	fileUpload();
        }
        else if (s.length>=2 && s[1].equals("fileuploaddone")) {
        	fileUploadDone();
        }
        else if (s.length>=2 && s[1].equals("download")) {
        	download();
        }
        else if (s.length>=2 && s[1].equals("search")) {
        	search();
        }
        else if (s.length>=2 && s[1].equals("browserjs")) {
        	browserjs();
        }
        else if (s.length>=2 && s[1].equals("import1")) {
        	importFromVer1();
        }
        else if (req.getPathInfo().equals("/")) {
        	topPage();
        }
        else {
        	byName();
        }
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

	private void download() throws IOException {
		// viewpoint: all
		// input param:
		//    since:   displays documents.lastupdate>since 
		// output:
		// SEPARATOR
		// document full(id/lastupdate included)
		// SEPARATOR
		// document full
		String sep="--------vfdes0ivgergu934"+"jgt3409gjp9odfgkpdigpo"+Math.random()+"dig09-erfjigfer-04t040g0tgj";
		long since=Long.parseLong( params().get("since") );
		/*for (IDocumentRef r:mountAll()) {
			if (r.lastUpdate()>since) {
				print(sep+"\n");
				DocumentProcessor p=documentProcessor(r.document());
				p.feedMetaHeadBody();
			} else break;
		}*/
	}
	private void upload() throws IOException {
		// viewpoint: all
		// input:
		// data=SEPARATOR
		// document full
		// SEPARATOR
		// document full
		// output:
		// none
		if ("get".equalsIgnoreCase( req.getMethod())){
			print(Html.p("<html><body>" +
					"<form action=%a method=POST><textarea name=data><input type=submit></form>" +
					"</body></html>",
					rootPath()+"/upload"));
		} else {
			String data=params().get("data");
			
		}
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
		all();
	}
	private void byId() throws IOException {
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
			boolean execed=false;
			Object doGet = ScriptableObject.getProperty(d, DOGET);
			if (doGet instanceof Function ) {
				final Function f=(Function) doGet;
				/*JSSession jsSession = jssession();
				jsSession.call(f, new Object[]{getReq(),getRes()});
				*/
				JSSession.withContext(new ContextRunnable() {
					
					@Override
					public Object run(Context cx) {
						f.call(cx, jssession().root, d, new Object[]{getReq(),getRes()});
						return null;
					}
				});
				execed=true;
			}
	        if (!execed) {
	        	print(id+" is not executable :"+d);
	        }
		} else {
			 notfound(id);
		}
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
	@Deprecated
	private void byName() throws IOException {
		String str=req.getPathInfo().replaceAll("^/", "");
		Query q=QueryBuilder.create("name:?").tmpl("name", str, AttrOperator.exact).toQuery();
		final Ref<Boolean> found=new Ref<Boolean>(false);
		documentLoader.searchByQuery(q, new BuiltinFunc() {
			
			@Override
			public Object call(Context cx, Scriptable scope, Scriptable thisObj,
					Object[] args) {
				DocumentScriptable s=(DocumentScriptable)args[0];
				try {
					documentProcessor(s).proc();
					found.set(true);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
			}
		});
		if (!found.get()) {
			notfound(str);
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
			ContentChecker c=new ContentChecker(content,addedVars());
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
				"Content: <br/>\n"+
				"<textarea id=edit name=%a rows=25 cols=60>%t</textarea>"+
				"<input type=submit>"+
				"</form>"+indentAdap()+"</body></html>",
				 "./new", msg, ATTR_PRECONTENT, ATTR_CONTENT , content)
		);
	}
	private String indentAdap() {
		return  Html.p("<script src=%a></script><script>attachIndentAdaptor('edit')</script>"
				,browserjsPath(IndentAdaptor.class));
	}
	private void edit() throws IOException {
		String[] s=args();
		//   $soyText/edit/00000
		String id=s[2];
		String msg="";
		DocumentScriptable d = documentLoader.byId(id);
		if (d==null) {
		    notfound(id);
		    return;
		}
		String content = d.getDocument().content;	
		if (req.getMethod().equals("POST")) {
			content=params().get(ATTR_CONTENT);
			ContentChecker c=new ContentChecker(content,addedVars());
			if (c.check()) {
				documentProcessor(d).proc();
				return;
			}
			content=c.getChangedContent();
			msg=contentStatus(c);
		}
		String preContent = d.getDocument().preContent;
		Httpd.respondByString(res, menuBar()+Html.p(
				"<form action=%a method=POST>%s"+
				"<!--preContent: <br/>\n"+
				"<input name=%a value=%a /><br/-->"+
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
					"<textarea name=%a rows=20 cols=80>%t</textarea>"+
					"<input type=submit>"+
					"</form></body></html>","./"+id, HttpContext.ATTR_BODY, d.get(ATTR_BODY)+"")
			);
		}
	}
    private void auth() throws IOException {
		/*String user=params().get("user");
		String pass=params().get("pass");
		if ("logout".equals(user) || (user!=null && user.length()>0 && pass!=null && pass.length()>0)) {
			Session s;
			if ("logout".equals(user)) {
				user="";
				s=sessionManager().defaultSession();
			}  else {
				try {
					s=sessionManager().create(user,pass);
				} catch (AuthException e) {
		    		Httpd.respondByString(res, e.getMessage());
		    		return;
				}
			}
    		res.addCookie(new Cookie(SESSION_NAME, s.id()));
    		//res.setHeader("Location", rootPath()+"/all");
    		//Httpd.respondByString(res, menuBar()+"Logged in");
    		String after=params().get("after");
    		if (after!=null) {
    			res.sendRedirect(rootPath()+"/"+after);
    		} else {
    			res.sendRedirect(rootPath()+"/");
    		}
    	} else {
    		Httpd.respondByString(res, "<form action=\"./auth\" method=\"POST\">"+
    				"ユーザ名： <input name=\"user\" value=\""+user+"\"><br/>"+
    				"パスワード: <input type=\"password\" name=\"pass\">"+
    				"<br><input type=submit>"+
    				"</form>"
    		);
    	}*/
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
	private String linkBar(DocumentScriptable ds) {
		return linkBar(ds,null);
	}
	private String linkBar(DocumentScriptable ds,String sel) {
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
				"<a href=%a>Exec</a> "+
				"<a href=%a>NewObj</a> "+
				"%t<br/>\n"
				, AJAXTAG+id
				, selt
				, rootPath()+"/byId/"+id 
				, rootPath()+"/edit/"+id 
				, rootPath()+"/editbody/"+id 
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
    	res.setContentType ("text/html; charset=utf-8");
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
		String path=rootPath();
		StringBuilder buf=new StringBuilder();
        buf.append(Html.p("<html><head><meta http-equiv=%a content=%a></head>"
        		                ,"Content-type","text/html; charset=utf8"));
        buf.append("<body>");
        //buf.append("User: "+currentSession().id()+" | ");
        buf.append(Html.p("<a href=%a>ログイン</a>  |" , path+"/auth"));
        buf.append(Html.p("<a href=%a>ホーム</a>  |" , path+"/all"));
        buf.append(Html.p("<a href=%a>新規作成</a> | ", path+"/new"));
        buf.append(Html.p("<a href=%a>検索</a><hr/>\n" , path+"/search"));
        return buf.toString();
	}
	
	/*public static String lastModifiedField(Document d)
	{
	    return TimeFormat.toRFC2822(d.lastUpdate());//TimeFormat.toUtcTicks(d.lastUpdate(), TimeZone.getDefault())); 
	}*/
	public static String detectContentType(String fileName) {
		String c = "text/plain; charset=utf-8";
	    if (fileName != null)
	    {
	    	fileName=fileName.toLowerCase();
	        if (fileName.endsWith(".js"))
	        {
	            c = "text/javascript; charset=utf-8";
	        }
	        if (fileName.endsWith(".css"))
	        {
	            c = "text/css; charset=utf-8";
	        }
	        if (fileName.endsWith(".html"))
	        {
	            c = "text/html; charset=utf-8";
	        }
	        if (fileName.endsWith(".gif"))
	        {
	            c = "image/gif";
	        }
	        if (fileName.endsWith(".png"))
	        {
	            c = "image/png";
	        }
	        if (fileName.endsWith(".jpg"))
	        {
	            c = "image/jpeg";
	        }
	    }
	    return c;		
	}
	
	public static String detectContentType(DocumentScriptable d)
	{
	    Object c = d.get("Content-Type");
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
