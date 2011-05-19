package jp.tonyu.soytext2.servlet;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.tonyu.soytext2.document.Document;
import jp.tonyu.soytext2.document.DocumentSet;
import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.util.Literal;



public class DocumentProcessor {
	//private static final String STRICTLY_PARSED = "_strictlyParsed";
	DocumentScriptable d;
	HttpContext ctx;
	public DocumentProcessor(DocumentScriptable d, HttpContext ctx) {
		super();
		this.d = d;
		this.ctx = ctx;
	}
	public HttpServletRequest req() {return ctx.getReq();}
	public HttpServletResponse res() {return ctx.getRes();}
	public DocumentSet documentSet() {return ctx.documentSet();}
	/*public void execHtml_old() throws IOException {
		Doc2Dtl d2d=null;
		Doc2JS d2j=null;
		for (Evaluator e: (MultiEvaluator)ctx.evaluator()) {
			if (e instanceof Doc2Dtl) {
				d2d = (Doc2Dtl) e;
			}
			if (e instanceof Doc2JS) {
				d2j = (Doc2JS) e;
			}
		}
		Debug.notNull(d2d,"d2d");
		Debug.notNull(d2j,"d2j");
		//Doc2Dtl d2d=ctx.doc2dtl();
		if (d2d.isEvaluatable(d)) {execHtmlAsDtl(d2d);return;}
		//Doc2JS d2j=ctx.doc2js();
		if (d2j.isEvaluatable(d)) {execHtmlAsJS(d2j);return;}

		ctx.frobidden(d+" is not execable");
	}*/
	//public static final String ARGUMENTORDER = "arguments";
	public static final Pattern docPat=Pattern.compile("\\[\\[([^\\[\\]]+)*\\]\\]");
	public void execHtml() throws IOException {
		/*Value e;
		res().setContentType( HttpContext.detectContentType(d) );
		String de=d.str("Content-Disposition");
		if (de!=null) {
			Debug.syslog("Set-content-disp = "+de);
			res().setHeader("Content-Disposition", de);
		}
		try {
			CompileResult r= ctx.evaluator().compile(d);
			e=r.toValue();
		} catch (CompileException e2) {
			ctx.print(e2.getMessage());
			e2.printStackTrace();
			return ;
		}
		if (e==null) Debug.die(d+" is not evaluatable.");
		
		
		String[] path=ctx.args(); //   /exec/id/params/params...

		int argCnt=3;
		Context args=new Context();
		ParsedDocument tmples=d.parsed();
		for (String key:tmples.keys()) {
			String tmplValue=tmples.get(key);
			StringValue sk=new StringValue(key);
			//Debug.syslog(" getV "+getV+"  "+path+"  "+argCnt);
			if (tmplValue.startsWith("?")) {
				String paramValue=params().get(key);
				if (paramValue==null && argCnt<path.length) {
					paramValue=path[argCnt];argCnt++;
					//Debug.syslog("getV "+getV);
				}
				if (tmplValue.startsWith("?doc")) {
					Value v2 = (  documentSet().byId(paramValue  ));
					args.put(sk,v2);
					args.add( v2 );
				} else if (tmplValue.startsWith("?str")) {
					StringValue v2 = new StringValue( paramValue);
					//Debug.syslog("Put "+sk+"="+v2);
					args.put(sk,v2);
					args.add( v2);
				} else  {
					Value v2 = Document.parseValue( paramValue, documentSet());
					args.put(sk,v2);
					args.add( v2);
				}
			}
		}
		Debug.syslog("params = "+params());
		boolean completeArg=true;
		if (tmples.get(ARGUMENTORDER)!=null) {
			String[] argNames=tmples.get(ARGUMENTORDER).split("[ \u3000,]+");
			for (String key:argNames) {
				String v=params().get(key);
				if (v!=null) {
					args.add(Document.parseValue(v,documentSet()));
					//	args.add(new StringValue(v));
				} else {
					Debug.syslog("Arguments missing!");
					args.add(null);
					completeArg=false;
				}
			}
		}
		Debug.syslog("Passing arg= "+args);
		try {
			e.exec(args);
		} catch (ExecutionException e1) {
			Debug.syslog("LastExecerror = "+e1.getMessage());
			ctx.print( e1.getMessage());
		}*/
	}
	public Map<String, String> params() {
		return ctx.params();
	}
	void feedBody() throws IOException {
		HttpServletRequest req=req();
		HttpServletResponse res=res();

		res.setContentType( HttpContext.detectContentType(d) );
	   // String lastup = HttpContext.lastModifiedField(d);
	   // res.setHeader( "Last-Modified" ,  lastup);
	    
	    /*InputStream in = d.blob();
	    
	    if (in != null)
	    {
	    	byte[] buf= new byte[1024];
	    	while (true) {
	    		int r=in.read(buf);
	    		if (r<=0) break;
	    		res.getOutputStream().write(buf,0,r);
	    	}
	    }
	    else
	    {*/
		//Log.d(this, d.getDocument().id+" cont="+d.getDocument().content+" - "+d.get(HttpContext.bodyAttr));
	        Object body = d.get(HttpContext.ATTR_BODY);
			Httpd.respondByString(res, body+"");
	    //}
	}
	/*void feedHead() throws IOException
	{
		HttpServletRequest req=req();
		HttpServletResponse res=res();

	    String c = "text/plain; charset=utf-8";
	    res.setContentType(c);
	    //res.setHeader("Last-Modified", HttpContext.lastModifiedField(d));
	    Httpd.respondByString(res, d.head());
	}
	void feedHeadBody() throws IOException
	{
		HttpServletRequest req=req();
		HttpServletResponse res=res();

	    String c = "text/plain; charset=utf-8";
	    res.setContentType (c);
	    //res.setHeader("Last-Modified", HttpContext.lastModifiedField(d));
	    Httpd.respondByString(res, d.content());
	}*/
	void feedJSON() throws IOException
	{
		HttpServletRequest req=req();
		HttpServletResponse res=res();

	    String c = "text/plain; charset=utf-8";
	    res.setContentType (c);
	    //res.setHeader("Last-Modified", HttpContext.lastModifiedField(d));
	    String meta="id: "+id()+"\n"+"lastupdate: "+d.getDocument().lastUpdate+"\n";
	    Httpd.respondByString(res, meta+d.getDocument().content);
	}
	@Deprecated
	void feedMetaHeadBody() throws IOException
	{
		HttpServletRequest req=req();
		HttpServletResponse res=res();

	    String c = "text/plain; charset=utf-8";
	    res.setContentType (c);
	    //res.setHeader("Last-Modified", HttpContext.lastModifiedField(d));
	    String meta="id: "+id()+"\n"+"lastupdate: "+d.getDocument().lastUpdate+"\n";
	    StringBuilder b=new StringBuilder();
	    for (Object id:d.getIds()) {
	    	Object value=d.get(id);
	    	String ids=id.toString();
	    	if (HttpContext.ATTR_BODY.equals(ids)) continue;
	    	if (value instanceof String) {
				b.append(ids+": "+value+"\n");
			}
	    	if (value instanceof DocumentScriptable) {
				DocumentScriptable scr = (DocumentScriptable) value;
				b.append(ids+": "+scr.getDocument().id+"\n");
			}
	    }
	    b.append("\n");
	    b.append(d.get(HttpContext.ATTR_BODY));
	    Httpd.respondByString(res, meta+b);
	}
	private String id() {
		return d.getDocument().id;
	}
	void proc() throws IOException
	{
		HttpServletRequest req=req();
		HttpServletResponse res=res();

		String[] args=args();
		String query=queryString();
		String format=ctx.params().get("_format");

		if (req.getMethod().equals("GET"))
		{

			String lm = req.getHeader("If-Modified-Since");
			if (lm != null)
			{
				long oneSec = 10000000;
				//   10^-9 = 0.000000001（10億分の1）
				// 100nano = 10^-7  
				// 1sec/100nano sec = 1 sec / (10^-7) sec = 10^7
				//long ifModifiedSince = req.getDateHeader(lm);//  TimeFormat.fromRFC2822(lm) + oneSec;
				//long lastModified = d.lastUpdate(); //  TimeFormat.toUtcTicks(d.lastUpdate(), TimeZone.getDefault());
				//long now = TimeFormat.utcnow();
				/*Debug.syslog(req.getQueryString() +
	                      "  lm: " + TimeFormat.toRFC2822(lastModified)+
	                      " ifm : " + TimeFormat.toRFC2822(ifModifiedSince) +
	                      "  now: " + TimeFormat.toRFC2822(now)
	                );*/
				/*if (lastModified <= ifModifiedSince && ifModifiedSince<now )
				{
					res.setStatus(304);
					Debug.syslog(req.getQueryString() + " not modified");
					return;
				}*/
			}
			/*if ("head".equals(query) || "head".equals(format))
			{
				feedHead();
			}
			else if ("headbody".equals(query) || "headbody".equals(format))
			{
				feedHeadBody();
			}
			*/
			if ("json".equals(query)) {
				feedJSON();
			} else if ("metaheadbody".equals(query) || "metaheadbody".equals(format)) // meta: id, lastupdate
			{
				feedMetaHeadBody();
			}
			else
			{
				feedBody();
			}
		}
		else if (req.getMethod().equals("POST"))
		{

			//String str = getContentStr(req);
			
			Map<String,String> keys=params();//  HttpUtility.ParseQueryString(str);
			if (keys.containsKey(HttpContext.ATTR_CONTENT)) {
				d.setContentAndSave(keys.get(HttpContext.ATTR_CONTENT));				
			} else {
				classicPost(d,keys);
			}
			res.setContentType("text/html; charset=utf8");
			String docBase=rootPath();
			Httpd.respondByString(res, Html.p(HttpContext.ajaxTag("id:"+id())+"\n Edit "+id()+" End <br/>\n"+
					"<a href=%a>Top</a>  "+
					"<a href=%a>View</a>  "+
					"<a href=%a>Edit</a> \n"+
					"<a href=%a>EditBody</a> \n"+
					"<a href=%a>Exec</a> <br/>\n", docBase+"/all" , docBase+"/byId/"+id(),
					docBase+"/edit/"+id(), docBase+"/editbody/"+id(), docBase+"/exec/"+id() ));

		}
	}
	@Deprecated
	private void classicPost(DocumentScriptable d, Map<String, String> keys) {
		for (String k :keys.keySet())
		{
			String v=keys.get(k);
			if (v==null) continue;
			Object vv=parseValue(v, d.getDocument().documentSet);
			d.put(k,vv);
		}
		d.save();
	}
	public String rootPath() {
		return ctx.rootPath();
	}
	public String[] args() {
		return ctx.args();
	}
	public String queryString() {
		return ctx.queryString();
	}
	public static final Pattern idpat=Pattern.compile("[0-9]+(_[0-9]+)+(@[0-9_a-zA-Z]+)?");
	public static final Pattern idpatWiki=Pattern.compile("\\[\\[([^\\]]+)\\]\\]");

	@Deprecated
	public static Object parseValue(String value,DocumentSet viewPoint) {
		if (value==null) return  null;
		Matcher ma=Literal.DQ.matcher(value);
		if (ma.lookingAt()) {
			return Literal.fromQuoteStrippedLiteral(ma.group(1));
		}
		ma=idpat.matcher(value);	
		Document dr=null;
		if (ma.lookingAt()) {
			try {
				dr=viewPoint.byId(value);
			}catch(Exception e){}
		}
		ma=idpatWiki.matcher(value);
		if (ma.lookingAt()) {
			dr=viewPoint.byId(ma.group(1));
		}		
		if (dr!=null) {
			return (dr);
		}
		return value;
	}
}
