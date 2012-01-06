package jp.tonyu.soytext2.servlet;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.tmatesoft.sqljet.core.SqlJetException;

import jp.tonyu.debug.Log;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.util.Context;
import jp.tonyu.util.SFile;

public class JarDownloader {
	public static final Context<String> jarFile=new Context<String>();
	public static void startDownload(HttpContext ctx, String dbid,SDB src, String[] ids) throws IOException, SqlJetException {
		if (jarFile.get().length()==0) Log.die("jar file not set");
		SFile inputJarFile=new SFile(jarFile.get());
		File outDbF=File.createTempFile("main.tmp",".db");
		SDB outdb=new SDB(outDbF);
		src.cloneWithFilter(outdb, ids);
		outdb.close();
		//SFile dbFile = new SFile( src.getFile() );
		//String dbid= Math.random()+".tonyu.jp";
		Log.d("DBDL", "dbid ="+dbid+" outDBF="+outDbF);
		//SFile outputJarFile=new SFile( File.createTempFile("main.tmp", ".db") );
		ctx.getRes().setContentType("Application/Octet-Stream");
		OutputStream out=ctx.getRes().getOutputStream();
		JarGenerator g = new JarGenerator(inputJarFile, out, new SFile(outDbF), dbid);
		g.generate();
		out.close();
	}
}
/*
2.2010.tonyu.jp@1.2010.tonyu.jp(client)

$.extend(_,{
scope: {
    
},
remoteLastSynced: 16842,
_sendSyncUrl: "http://localhost:8080/soytext2/sendsync?localsyncid=2.2010.tonyu.jp@1.2010.tonyu.jp&remotesyncid=1.2010.tonyu.jp@2.2010.tonyu.jp",
name: "2@1",
target: "2.2010.tonyu.jp",
remoteSyncId: "1.2010.tonyu.jp@2.2010.tonyu.jp",
localLastSynced: 16547,
url: "http://cho.is.meisei-u.ac.jp/soytext2/recvsync",
constructor: $.byId("1265@1.2010.tonyu.jp")
});


1.2010.tonyu.jp@2.2010.tonyu.jp(server)

$.extend(_,{
    scope: {
        
    },
    remoteLastSynced: 16479,
    name: "1@2",
    target: "1.2010.tonyu.jp",
    localLastSynced: 16842,
    constructor: $.byId("1265@1.2010.tonyu.jp")
});

*/