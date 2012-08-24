package jp.tonyu.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.util.Iterator;

public class SFile implements Iterable<SFile>{
	java.io.File f;
	public long lastModified() {
		if (!exists()) return 0;
		return f.lastModified();
	}
	public SFile(String path) {
		f=new java.io.File(path);
	}
	public SFile(java.io.File f) {
		this.f=f;
	}
	public SFile rel(String relPath) {
		return new SFile(new java.io.File(f,relPath));
	}
	public boolean exists() {
		return f.exists();
	}
	public String relPath(SFile base) {
	    if (equals(base)) {
	        return ".";
	    }
	    if (parent()==null) throw new RuntimeException(this+" is not in "+base);
	    return parent().relPath(base)+File.pathSeparator+name();
	}
	@Override
	public boolean equals(Object obj) {
	    if (obj instanceof SFile) {
            SFile s=(SFile) obj;
            return f.equals(s.f);
        }
	    return false;
	}
	@Override
	public int hashCode() {
	    return f.hashCode();
	}
    public String text() throws IOException {
        return textEnc("utf-8");
    }
    public String textEnc(String enc) throws IOException {
        if (!exists()) return null;
        BufferedReader rd=new BufferedReader(new InputStreamReader(new FileInputStream(f),enc));// FileReader(f));
        StringBuffer buf=new StringBuffer();
        String sep="";
        while (true) {
            String line=rd.readLine();
            if (line==null) break;
            buf.append(sep+line);
            sep="\n";
        }
        rd.close();
        return buf.toString();
    }
	public void text(String content) throws FileNotFoundException {
	    textEnc(content,"utf-8");
	}
	public void textEnc(String content,String encode) throws FileNotFoundException {
		PrintWriter p;
		try {
			p = new PrintWriter(new OutputStreamWriter(outputStream(), encode));
			p.print(content);
			p.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public boolean mkdirs(boolean toBeDir) {
	    if (exists()) {
	        return isDir()==toBeDir;
	    }
	    if (toBeDir) {
	        return f.mkdirs();
	    } else {
	        File parentFile = f.getParentFile();
	        if (parentFile!=null) return parentFile.mkdirs();
	        return false;
	    }
	}
	public boolean isDir() {
		return f.exists() && f.isDirectory();
	}
	@Override
	public Iterator<SFile> iterator() {
		final java.io.File[] files;
		if (!exists() || !isDir()) files=new java.io.File[0];
		else {
			files=f.listFiles();
		}

		return new Iterator<SFile>() {
			int i=0;
			@Override
			public boolean hasNext() {
				return i<files.length;
			}

			@Override
			public SFile next() {
				return new SFile(files[i++]);
			}

			@Override
			public void remove() {
				// TODO Auto-generated method stub

			}

		};
	}
	public String name() {
		return f.getName();
	}
	public java.io.File javaIOFile() {
		return f;
	}
	public InputStream inputStream() throws FileNotFoundException {
		return new FileInputStream(f);
	}
	@Override
	public String toString() {
		return javaIOFile().toString();
	}
	public OutputStream outputStream() throws FileNotFoundException {
		mkdirs(false);
		return new FileOutputStream(f);
	}
	public String fullPath() {
		try {
			return f.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public long size() {
		return f.length();
	}
	public String[] lines() throws IOException {
		return text().split("[\\r\\n]+");
	}
	// copy  path/of/this from another/path/of/dst
	// if true, New folder another/path/of/dst/this will created
    //       and path/of/this/a.txt will be copied into another/path/of/dst/this/a.txt
	// if false, path/of/this/a.txt will be copied into another/path/of/dst/a.txt
	public void copyDirTo(SFile dst, boolean createNewFolder) throws IOException {
	    if (!isDir()) throw new RuntimeException(this +" is not a dir");
	    SFile folder=(createNewFolder ? dst.rel(name()) : dst);
	    copyDir( this,folder);
	}
	public static void copyDir(SFile curSrc, SFile curDst ) throws IOException {
	    curDst.mkdirs(true);
	    for (SFile f:curSrc) {
	        SFile dstFile=curDst.rel(f.name());
            if (f.isDir()) {
	            copyDir(f, dstFile);
	        } else {
	            f.copyTo(dstFile);
	        }
	    }
	}

	public void copyTo(SFile dst) throws IOException {
		copy(javaIOFile(),dst.javaIOFile());
	}
	public void copyTo(File dst) throws IOException {
		copy(javaIOFile(),dst);
	}
	public void backup(String dir) throws IOException {
		SFile backupFile = backupFile(dir);
		copyTo(backupFile);
	}
	public boolean moveAsBackup(String dir) {
		return moveTo(backupFile(dir));
	}
	public SFile backupFile(String dir) {
		String d=new TDate().toString("yyyy_MMdd_HH_mm_ss_");
		SFile bdir=parent();
		if (dir!=null) bdir=bdir.rel(dir);
		SFile backupFile = bdir.rel(d+name());
		System.out.println("Backup is "+backupFile.javaIOFile().getAbsolutePath());
		return backupFile;
	}
	public SFile parent() {
		return new SFile(f.getParentFile());
	}
	public boolean moveTo(SFile dest) {
		if (dest.exists()) return false;
		if (isDir()) throw new RuntimeException("Dir is not supported");
		dest.mkdirs(false);
		boolean res=moveTo(dest.javaIOFile());
		if (!res) {
			try {
				//Log.d("move","copying to "+dest);
				copyTo(dest);
				//Log.d("move","deleting"+javaIOFile());
				res=javaIOFile().delete();
				//Log.d("move","done "+res);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return res;
	}
	public boolean moveTo(File dest) {
		return f.renameTo(dest);
	}
	public static void copy(File src,	File dest) throws IOException {
		long l=src.lastModified();
		FileChannel srcChannel = new
				FileInputStream(src).getChannel();
		FileChannel destChannel = new
				FileOutputStream(dest).getChannel();
		try {
			srcChannel.transferTo(0, srcChannel.size(), destChannel);
		} finally {
			srcChannel.close();
			destChannel.close();
		}
		dest.setLastModified(l);

	}
	public byte[] bytes() throws IOException {
		InputStream i = inputStream();
		byte[] res=new byte[(int)size()];
		i.read(res);
		i.close();
		return res;
	}
	public void bytes(byte[] b) throws IOException {
		OutputStream o = outputStream();
		o.write(b);
		o.close();
	}
	public void writeTo(OutputStream out) throws IOException {
		InputStream in = inputStream();
		redirect(in,out);
		in.close();
	}
	public void readFrom(InputStream in) throws IOException {
		OutputStream out = outputStream();
		redirect(in, out);
		out.close();
	}
	public static void redirect(InputStream in, OutputStream out) throws IOException {
		byte[] b=new byte[1024];
		while (true) {
			int r=in.read(b);
			if (r<=0) break;
			out.write(b,0,r);
		}
	}
    public void delete() {
        if (f.exists()) f.delete();
    }

}
