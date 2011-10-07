package jp.tonyu.soytext2.file;

import java.io.IOException;
import java.io.InputStream;

import jp.tonyu.js.Wrappable;

public class WrappedInputStream extends InputStream implements Wrappable {
	InputStream src;

	public WrappedInputStream(InputStream src) {
		super();
		this.src = src;
	}

	public int read() throws IOException {
		return src.read();
	}

	public int hashCode() {
		return src.hashCode();
	}

	public int read(byte[] b) throws IOException {
		return src.read(b);
	}

	public boolean equals(Object obj) {
		return src.equals(obj);
	}

	public int read(byte[] b, int off, int len) throws IOException {
		return src.read(b, off, len);
	}

	public long skip(long n) throws IOException {
		return src.skip(n);
	}

	public String toString() {
		return src.toString();
	}

	public int available() throws IOException {
		return src.available();
	}

	public void close() throws IOException {
		src.close();
	}

	public void mark(int readlimit) {
		src.mark(readlimit);
	}

	public void reset() throws IOException {
		src.reset();
	}

	public boolean markSupported() {
		return src.markSupported();
	}
	

}
