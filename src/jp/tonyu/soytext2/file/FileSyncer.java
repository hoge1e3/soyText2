package jp.tonyu.soytext2.file;

import java.io.IOException;
import java.io.InputStream;

import jp.tonyu.js.BlankScriptableObject;
import jp.tonyu.js.ContextRunnable;
import jp.tonyu.js.Scriptables;
import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.document.DocumentSet;
import jp.tonyu.soytext2.js.DBHelper;
import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.js.JSSession;
import jp.tonyu.soytext2.servlet.HttpContext;
import jp.tonyu.util.SFile;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class FileSyncer implements Wrappable {
	private static final String USE_BLOB_DIR = "useBlobDir";
	private static final String BASE64BODY = "base64body";
	private static final String SOY_TEXT_ID = "__soyText.id";
	private static final String FILES = "files";

	public Scriptable sync(DBHelper db, String dirPath,  Function fileFactory) throws IOException {
		SFile dir=new SFile(dirPath);
		return sync(db,dir,fileFactory);
	}
	private Scriptable sync(DBHelper db, SFile dir, Function fileFactory) throws IOException {
		Scriptable dirDoc=null;
		SFile stDesc=dir.rel(SOY_TEXT_ID);
		if (stDesc.exists()) {
			String id=stDesc.text().trim();
			dirDoc=(DocumentScriptable) db.byId(id);
		}
		if (dirDoc==null) {
			dirDoc=newDir(fileFactory);
			Object id=ScriptableObject.getProperty(dirDoc, "id");
			if (dirDoc instanceof DocumentScriptable) {
				DocumentScriptable ds = (DocumentScriptable) dirDoc;
				ds.save();
			}
			if (id instanceof String) {
				String sid = (String) id;
				stDesc.text(sid);
			}
		}
		return sync(db, dirDoc, dir, fileFactory);
	}

	private Scriptable sync(DBHelper db, Scriptable fileScr, SFile file, Function fileFactory) throws IOException {
		Object sName=ScriptableObject.getProperty(fileScr, "name");
		if (!(sName instanceof String)) {
			ScriptableObject.putProperty(fileScr, "name", file.name());
		}
		String cType=HttpContext.detectContentType(file.name(), "Application/Octet-Stream");
		if(cType!=null)ScriptableObject.putProperty( fileScr,  HttpContext.CONTENT_TYPE, cType);
		if (file.isDir()) {
			Scriptable files=Scriptables.getAsScriptable(fileScr, FILES);
			if (files==null) {
				files=new BlankScriptableObject();
				ScriptableObject.putProperty(fileScr, FILES, files);
			}

			for (SFile fileElem:file) {
				Scriptable scrElem=Scriptables.getAsScriptable(files, fileElem.name());
				if (scrElem!=null) {
					sync(db, scrElem, fileElem, fileFactory);
				} else {
					if (fileElem.isDir()) {
						scrElem=sync(db, fileElem, fileFactory);
					} else {
						scrElem=newFile(fileFactory);
						sync(db, scrElem, fileElem, fileFactory);
					}
				}
				ScriptableObject.putProperty(files, fileElem.name(), scrElem);
			}
		} else {
			if (cType.startsWith("text")) {
				ScriptableObject.deleteProperty(fileScr, BASE64BODY);
				ScriptableObject.putProperty(fileScr, HttpContext.ATTR_BODY, file.text());
			} else {

			    /*byte[] b=file.bytes();
				byte[] b64=BASE64EncoderStream.encode(b);
				String str=new String(b64, "utf-8");
				ScriptableObject.putProperty(fileScr, BASE64BODY, str );
				ScriptableObject.deleteProperty(fileScr, HttpContext.ATTR_BODY);*/


			}
		}
		if (fileScr instanceof DocumentScriptable) {
			DocumentScriptable d = (DocumentScriptable) fileScr;
			d.save();
		}
		return fileScr;
	}

	private Scriptable newDir(final Function factory) {
		Scriptable res = newFile(factory);
		ScriptableObject.putProperty(res, FILES, new BlankScriptableObject());
		return res;
	}
	private Scriptable newFile(final Function factory) {
		Scriptable res=(Scriptable)JSSession.withContext(new ContextRunnable() {
			@Override
			public Object run(Context cx) {
				return factory.call(cx, factory, factory, new Object[0]);
			}
		});
		return res;
	}
	/*public static void writeBase64(OutputStream w, String base64Data) throws IOException {
		byte[] dec=BASE64DecoderStream.decode(base64Data.getBytes("utf-8"));
		w.write(dec);
	}
	public static void writeBase64(OutputStream w, Scriptable file) throws IOException {
		Object property = ScriptableObject.getProperty(file, BASE64BODY);
		if (property instanceof String) {
			String sp = (String) property;
			writeBase64(w, sp);
		}
	}*/
	public static SFile getBlobFile(DocumentSet ds,Scriptable file) {
		// finds from  workspace/blob/id.ext
		String blobExt=Scriptables.getAsString(file, "blobExt", "");
		String fileId=Scriptables.getAsString(file, "id", null);
		/*if (ds instanceof SDB && fileId!=null) {
			SDB s=(SDB)ds;
			SFile f = s.getBlobDir().rel(fileId+blobExt);
			return f;
		}
		return null;*/
		return new SFile( ds.getBlob(fileId) );
	}
	public static void setBlob(DocumentScriptable file , InputStream str) throws IOException {
		DocumentSet ds = file.loader.getDocumentSet();
		SFile f=getBlobFile(ds,file);
		if (f!=null) {
			f.readFrom(str);
			return;
		}
	}
	public static void writeFile(HttpContext ctx, Scriptable file) throws IOException {
		String cType=Scriptables.getAsString(file, HttpContext.CONTENT_TYPE, null);
		//HttpContext.TEXT_PLAIN_CHARSET_UTF_8
		if (cType==null) {
			String name=Scriptables.getAsString(file, "name", "");
			cType=HttpContext.detectContentType(name);
		}
		ctx.getRes().setContentType(cType);
		/*Object useBlobDir = ScriptableObject.getProperty(file, USE_BLOB_DIR);
		if ("true".equals(useBlobDir)) {*/
		DocumentSet ds=ctx.documentSet();
		SFile f=getBlobFile(ds,file);
		if (f!=null && f.exists()) {
			f.writeTo(ctx.getRes().getOutputStream());
		/*} else 	if (file.has(BASE64BODY, file)){
			writeBase64(ctx.getRes().getOutputStream(),file);*/
		} else {
			ctx.getRes().getWriter().print(Scriptables.getAsString(file, HttpContext.ATTR_BODY,""));
		}

	}
}