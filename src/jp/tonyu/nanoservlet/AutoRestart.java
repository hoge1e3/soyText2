package jp.tonyu.nanoservlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import jp.tonyu.debug.Log;
import jp.tonyu.util.SFile;

public class AutoRestart {
	File lockFile;
	String key;
	int port;
	public AutoRestart(int port, File lockFile) {
		this.lockFile=lockFile;
		this.port=port;
		start();
	}
	public void start() {
		if (lockFile.exists()) {
			try {
				key=new SFile(lockFile).text();
				URL u=new URL("http://localhost:"+port+"/"+key);
				InputStream i=(InputStream)u.getContent(new Class[]{InputStream.class});
				i.close();
				while (lockFile.exists()) {
					Log.d(this, "Waiting for shutdown...");
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		key=Math.random()+"";
		try {
			new SFile(lockFile).text(key);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}
	public void stop() {
		onStop();
		lockFile.delete();
	}
	protected void onStop() {
		
	}
	public boolean hasToBeStopped(String path) {
		return path.startsWith("/"+key);
	}
	public String stopURL() {
		return "http://localhost:"+port+"/"+key;
	}

}
