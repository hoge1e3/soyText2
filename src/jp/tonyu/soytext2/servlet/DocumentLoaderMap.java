package jp.tonyu.soytext2.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import jp.tonyu.soytext2.js.DocumentLoader;

public class DocumentLoaderMap {
	Map<String, DocumentLoader> h=new HashMap<String, DocumentLoader>();
	Workspace workspace;
	public DocumentLoaderMap(Workspace workspace) {
		super();
		this.workspace = workspace;
	}
	public DocumentLoader getDocumentLoader(String dbid) throws SQLException, IOException, ClassNotFoundException {
		DocumentLoader res = h.get(dbid);
		if (res!=null) return res;
		res=new DocumentLoader(workspace.getDB(dbid));
		h.put(dbid, res);
		return res;
	}
}
