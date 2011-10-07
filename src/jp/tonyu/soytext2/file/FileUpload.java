package jp.tonyu.soytext2.file;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.js.MapScriptable;
import jp.tonyu.soytext2.servlet.HttpContext;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.mozilla.javascript.Scriptable;


public class FileUpload implements Wrappable {
	public static Scriptable receiveFile(HttpContext c) {
		HttpServletRequest req=c.getReq();
		MapScriptable res=new MapScriptable(new HashMap<String,Object>());
		try {
			DiskFileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			factory.setSizeThreshold(102400);
			upload.setSizeMax(-1);
			//upload.setHeaderEncoding("utf-8");
			List<FileItem> list = upload.parseRequest(req);
			Iterator<FileItem> iterator = list.iterator();
			while(iterator.hasNext()){
				FileItem fItem = iterator.next();
				if(!(fItem.isFormField())){
					String fileName = fItem.getName();
					if((fileName != null) && (!fileName.equals(""))){
						fileName=(new File(fileName)).getName();
						InputStream str=fItem.getInputStream();
						res.put(fileName, new WrappedInputStream(str));
					}
				}else {
					res.put(fItem.getFieldName(),fItem.getString());
				}
			}
		}catch (FileUploadException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

}
