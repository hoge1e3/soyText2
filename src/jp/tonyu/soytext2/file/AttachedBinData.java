package jp.tonyu.soytext2.file;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jp.tonyu.js.Wrappable;
import jp.tonyu.util.SFile;

public class AttachedBinData implements BinData, Wrappable {
	SFile src;

	@Override
	public InputStream getInputStream() throws IOException {
		return new WrappedInputStream(src.inputStream());
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return src.outputStream();
	}

	public AttachedBinData(SFile src) {
		super();
		this.src = src;
	}

}
