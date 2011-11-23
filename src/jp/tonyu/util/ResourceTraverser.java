package jp.tonyu.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

public abstract class ResourceTraverser {
	ClassLoader cl=this.getClass().getClassLoader();
	public void traverse(String name) throws IOException {
		if (!isDir(name)) traverseAsFile(name);
		else traverseAsDir(name);
	}
	public void traverseAsFile(String name) throws IOException {
		//InputStream i = getInputStream(name);
		visitFile(name);
	}
	protected InputStream getInputStream(String name) {
		InputStream i=cl.getResourceAsStream(name);
		return i;
	}
	protected boolean isDir(String name) {
		return name.indexOf(".")<0;
	}
	protected abstract void visitFile(String name) throws IOException;
	protected boolean visitDir(String name, List<String> files)  throws IOException{
		return false;
	}
	public void traverseAsDir( String name) throws IOException {
		InputStream resStr = cl.getResourceAsStream(name);
		if (resStr==null) {
			System.out.println(name+" is not found.");
			return;
		}
		Scanner s=new Scanner(resStr);
		Vector<String> files=new Vector<String>();
		while (s.hasNextLine()) {
			String r=s.nextLine();
			files.add(r);
		}
		s.close();
		if (!visitDir(name, files)) {
			for (String n:files) {
				traverse(name+"/"+n);
			}
		}
	}
}
