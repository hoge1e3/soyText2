package jp.tonyu.soytext2.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jp.tonyu.js.Wrappable;
import jp.tonyu.util.SFile;

public class ZipMaker implements Wrappable {
	OutputStream out;
	ZipOutputStream zout;
	public ZipMaker(OutputStream out) {
		super();
		this.out = out;
		zout=new ZipOutputStream(out);
	}
    public void add(String path, InputStream data) throws IOException {
        ZipEntry zipEntry = new ZipEntry(path);
        zout.putNextEntry(zipEntry);
        SFile.redirect(data, zout);
    }
	public void add(String path, BinData data) throws IOException {
		ZipEntry zipEntry = new ZipEntry(path);
		zout.putNextEntry(zipEntry);
		SFile.redirect(data.getInputStream(), zout);
	}
	public void add(String path, String data) throws IOException {
		ZipEntry zipEntry = new ZipEntry(path);
		zout.putNextEntry(zipEntry);
		byte[] b=data.getBytes("utf-8");
		zout.write(b);
	}

	public void close() throws IOException {
		zout.close();
	}
}
