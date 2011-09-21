package jp.tonyu.soytext2.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import jp.tonyu.js.BlankScriptableObject;
import jp.tonyu.js.ContextRunnable;
import jp.tonyu.js.Scriptables;
import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.js.DBHelper;
import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.js.JSSession;
import jp.tonyu.soytext2.servlet.HttpContext;
import jp.tonyu.util.SFile;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.UniqueTag;

import com.sun.mail.util.BASE64DecoderStream;
import com.sun.mail.util.BASE64EncoderStream;

public class FileSyncer implements Wrappable {
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
		String cType=HttpContext.detectContentType(file.name());
		ScriptableObject.putProperty( fileScr,  HttpContext.CONTENT_TYPE, cType);
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
			ByteArrayOutputStream out=new ByteArrayOutputStream();
			BASE64EncoderStream enc=new BASE64EncoderStream(out);
			file.writeTo(enc);
			enc.close();
			String str=new String(out.toByteArray(), "utf-8");
			ScriptableObject.putProperty(fileScr, BASE64BODY, str );
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
	public static void writeBase64(OutputStream w, String base64Data) throws IOException {
		byte[] dec=BASE64DecoderStream.decode(base64Data.getBytes("utf-8"));
		w.write(dec);
	}
	public static void writeBase64(OutputStream w, Scriptable file) throws IOException {
		Object property = ScriptableObject.getProperty(file, BASE64BODY);
		if (property instanceof String) {
			String sp = (String) property;
			writeBase64(w, sp);
		}
	}
	public static void writeBase64(HttpContext ctx, Scriptable file) throws IOException {
		writeBase64(ctx.getRes().getOutputStream(),file);
	}
}