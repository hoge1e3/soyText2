package jp.tonyu.soytext2.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ReadableBinData {
	public InputStream getInputStream() throws IOException;
}
