package jp.tonyu.soytext2.document;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.file.BinData;
import jp.tonyu.soytext2.file.ReadableBinData;
import jp.tonyu.soytext2.file.WrappedInputStream;

public interface HashBlob extends ReadableBinData, Wrappable {
    /*DocumentSet s;
    String hash;

    public HashBlob(DocumentSet s, String hash) {
        super();
        this.s=s;
        this.hash=hash;
    }
    @Override
    public InputStream getInputStream() throws IOException {
        return new  WrappedInputStream( s.getHashBlob(hash).getInputStream() );
    }
    public String getHash() {
        return hash;
    }*/
    public String getHash();
    public boolean exists();
    public String text();
    /*
     public String text() {  StringgetInputStream().
     */

}
