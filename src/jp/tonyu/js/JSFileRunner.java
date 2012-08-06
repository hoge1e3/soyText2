package jp.tonyu.js;

import java.io.IOException;

import jp.tonyu.debug.Log;
import jp.tonyu.util.SFile;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class JSFileRunner {
    Context ctx;
    Scriptable root;
    public JSFileRunner()  {
        ctx=Context.enter();
        root=ctx.initStandardObjects();
        ScriptableObject.putProperty(root, "sys", this);
        ScriptableObject.putProperty(root, "console", this);
    }
    public void log(Object s) {
        Log.d("log", s);
    }
    public void close() {
        Context.exit();
    }
    public Object load(SFile file) throws IOException {
        return ctx.evaluateString(root, file.text(), file.fullPath(), 1, null);
    }
    public Object load(String fileName) throws IOException {
        return load(new SFile(fileName));
    }
    public String fileText(String fileName) throws IOException {
        return new SFile(fileName).text();
    }
    public Object call(Function f,Scriptable self,Object... args) {
        return f.call(ctx, root, self, args);
    }
    public Object call(Function f, Object...args ) {
        return call(f, root, args);
    }
}
