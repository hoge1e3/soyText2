package jp.tonyu.soytext2.file;

import java.io.IOException;

import jp.tonyu.js.BlankScriptableObject;
import jp.tonyu.js.ContextRunnable;
import jp.tonyu.soytext2.js.DBHelper;
import jp.tonyu.soytext2.js.DocumentScriptable;
import jp.tonyu.soytext2.js.JSSession;
import jp.tonyu.util.SFile;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class FileSyncer {
	private static final String SOY_TEXT_ID = "__soyText.id";
	private static final String FILES = "files";

	public void sync(DBHelper db, String dirPath,  Function fileFactory) throws IOException {
		SFile dir=new SFile(dirPath);
		sync(db,dir,fileFactory);
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
		}
		return sync(db, dirDoc, dir, fileFactory);
	}

	private Scriptable sync(DBHelper db, Scriptable dirScr, SFile file, Function fileFactory) throws IOException {
		if (file.isDir()) {
			Scriptable files=(Scriptable)ScriptableObject.getProperty(dirScr, FILES);
			if (files==null) files=new BlankScriptableObject();
			ScriptableObject.putProperty(dirScr, FILES, files);

			for (SFile fileElem:file) {
				Scriptable scrElem=(Scriptable)ScriptableObject.getProperty(files, fileElem.name());
				if (scrElem!=null) {
					if (fileElem.isDir()) {
						sync(db, scrElem, fileElem, fileFactory);
					} else {
						sync(db, scrElem, fileElem, fileFactory);
					}
				} else {
					if (fileElem.isDir()) {
						scrElem=sync(db, fileElem, fileFactory);
					} else {
						scrElem=newFile(fileFactory);
						sync(db, scrElem, fileElem, fileFactory);
					}
					ScriptableObject.putProperty(files, fileElem.name(), scrElem);
				}
			}
		} else {
			//file
		}
		return dirScr;
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

}
