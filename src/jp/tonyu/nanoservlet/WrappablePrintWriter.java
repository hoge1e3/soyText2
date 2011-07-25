package jp.tonyu.nanoservlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import jp.tonyu.js.Wrappable;

public class WrappablePrintWriter extends PrintWriter implements Wrappable {

	public WrappablePrintWriter(File file, String csn)
			throws FileNotFoundException, UnsupportedEncodingException {
		super(file, csn);
		// TODO Auto-generated constructor stub
	}

	public WrappablePrintWriter(File file) throws FileNotFoundException {
		super(file);
		// TODO Auto-generated constructor stub
	}

	public WrappablePrintWriter(OutputStream out, boolean autoFlush) {
		super(out, autoFlush);
		// TODO Auto-generated constructor stub
	}

	public WrappablePrintWriter(OutputStream out) {
		super(out);
		// TODO Auto-generated constructor stub
	}

	public WrappablePrintWriter(String fileName, String csn)
			throws FileNotFoundException, UnsupportedEncodingException {
		super(fileName, csn);
		// TODO Auto-generated constructor stub
	}

	public WrappablePrintWriter(String fileName) throws FileNotFoundException {
		super(fileName);
		// TODO Auto-generated constructor stub
	}

	public WrappablePrintWriter(Writer out, boolean autoFlush) {
		super(out, autoFlush);
		// TODO Auto-generated constructor stub
	}

	public WrappablePrintWriter(Writer out) {
		super(out);
		// TODO Auto-generated constructor stub
	}

}
