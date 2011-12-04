package jp.tonyu.soytext2.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.util.SFile;

public class JarGenerator {
	SFile inputJarFile, outputJarFile;
	SFile dbFile;
	String dbid;
	JarInputStream in;
	JarOutputStream out;
	public void generate() throws IOException {
		in=new JarInputStream(inputJarFile.inputStream());
		Manifest m = in.getManifest();
		out=new JarOutputStream(outputJarFile.outputStream(),m);
		boolean putdb=false;
		while(true){
			ZipEntry e = in.getNextJarEntry();
			if (e==null) break;
			System.out.println(e.getName());
			if (e.getName().startsWith(SMain.DB_INIT_PATH)) {
				if (!putdb){
					putdb=true;
					ZipEntry prim = new ZipEntry(SMain.DB_INIT_PATH+"/"+SDB.PRIMARY_DBID_TXT);
					out.putNextEntry(prim);
					out.write(dbid.getBytes());
					ZipEntry maindb= new ZipEntry(SMain.DB_INIT_PATH+"/main.db");
					out.putNextEntry(maindb);
					dbFile.writeTo(out);
				}
			} else {
				out.putNextEntry(e);
				int r;
				byte[] buf=new byte[1024];
				while ((r=in.read(buf))>0) {
					out.write(buf,0,r);
				}
			}
		}
		out.close();
		in.close();
	}
	public static void main(String[] args) throws IOException {
		JarGenerator j = new JarGenerator();
		j.inputJarFile=new SFile("C:/Users/shinya/Dropbox/Downloads/drt.jar");
		j.outputJarFile=new SFile("C:/bin/Downloads/tmp/drt.out.jar");
		j.dbid="test.tonyu.jp";
		j.dbFile=new SFile("C:/Users/shinya/Dropbox/workspace/Dtl2Rhino/db/main.db");
		j.generate();
	}
}
