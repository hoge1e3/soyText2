package jp.tonyu.soytext2.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import jp.tonyu.debug.Log;
import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.js.MapScriptable;
import jp.tonyu.soytext2.servlet.HttpContext;
import jp.tonyu.util.A;
import jp.tonyu.util.Maps;
import jp.tonyu.util.SFile;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.mozilla.javascript.Scriptable;


public class FileUpload implements Wrappable {
    private static final String BODY="body";
    private static final String FILENAME="filename";

    public static Scriptable receiveFile(HttpContext c) throws FileUploadException, IOException {
        HttpServletRequest req=c.getReq();
        Log.d("FileUpload", req.getClass());
        if (req instanceof FileProperty) {
            FileProperty f=(FileProperty) req;
            return recieveFromFileProp(f);
        }
        MapScriptable res=new MapScriptable(new HashMap<String,Object>());
        //if (req.getContentType().indexOf("multipart-form-data")<0) return res;
        //try {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        factory.setSizeThreshold(102400);
        upload.setSizeMax(-1);
        //req.setCharacterEncoding("utf-8");
        List<FileItem> list = upload.parseRequest(req);
        Iterator<FileItem> iterator = list.iterator();
        while(iterator.hasNext()){
            FileItem fItem = iterator.next();
            if(!(fItem.isFormField())){
                String fileName = fItem.getName();
                if((fileName != null) && (!fileName.equals(""))){
                    fileName=(new File(fileName)).getName();
                    InputStream str=fItem.getInputStream();
                    HashMap<String, Object> vsrc=new HashMap<String, Object>();
                    vsrc.put(FILENAME, fileName);
                    vsrc.put(BODY, new WrappedInputStream(str));
                    res.put(fItem.getFieldName(), new MapScriptable(vsrc));
                }
            }else {
                byte[] bytes= fItem.getString().getBytes("iso-8859-1");
                String val= new String(bytes, "utf-8");

                res.put(fItem.getFieldName(),val);
            }
        }
        /*}catch (FileUploadException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }*/
        return res;
    }

    private static Scriptable recieveFromFileProp(FileProperty f) throws FileNotFoundException {
        Properties files=f.getFiles();
        Properties params=f.getParams();
        MapScriptable res=new MapScriptable(new HashMap<String,Object>());
        for (Object n:params.keySet()) {
            String v=params.getProperty(n+"");
            String fname=files.getProperty(n+"");
            if (fname==null) res.put(n, v);
            else {
                InputStream str=new SFile(fname).inputStream();
                HashMap<String, Object> vsrc=new HashMap<String, Object>();
                vsrc.put(FILENAME, v);
                vsrc.put(BODY, new WrappedInputStream(str));
                res.put(n, new MapScriptable(vsrc));
            }
        }
        return res;
    }

    public static Scriptable receiveFile_old(HttpContext c) {
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
