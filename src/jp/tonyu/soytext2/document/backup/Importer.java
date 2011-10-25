package jp.tonyu.soytext2.document.backup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tmatesoft.sqljet.core.SqlJetException;

import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.document.DocumentSet;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.document.LogRecord;
import jp.tonyu.soytext2.js.DocumentLoader;
import jp.tonyu.util.Literal;
import jp.tonyu.util.SFile;


public class Importer {
	final SDB sdb;
	final DocumentLoader documentLoader;
	String curTable=null;
	DocumentRecord curdoc=null;
	LogRecord curlog=null;
	public static final Pattern table=Pattern.compile("\\[([\\d\\w]+)\\]");
	public static final Pattern field=Pattern.compile("([\\d\\w]+): (.*)");
	public void importDocuments(File file) throws IOException,SqlJetException {
		Scanner s=new Scanner(file);
		curTable=null;
		curdoc=null;
		curlog=null;
		while (s.hasNextLine()) {
			String line=s.nextLine();
			if (curTable==null) {
				tryChangeTable(line);
			} else if ("UID".equals(curTable)) {
				//sdb.setDBID(line);
				tryChangeTable(line);
			} else if ("Document".equals(curTable)) {
				Matcher m=field.matcher(line);
				if (m.matches()) {
					String key=m.group(1).toLowerCase();
					String value=Literal.fromLiteral(m.group(2));
					if ("id".equals(key)) {
						flush();
						curdoc=getdoc(value);
					}
					if ("summary".equals(key)) {
						curdoc.summary=value;
					}
					if ("content".equals(key)) {
						curdoc.content=value;
					}
					if ("precontent".equals(key)) {
						curdoc.preContent=value;
					}				
					if ("owner".equals(key)) {
						curdoc.owner=value;
					}
					if ("group".equals(key)) {
						curdoc.group=value;
					}
					if ("permission".equals(key)) {
						curdoc.permission=value;
					}
				} else {
					tryChangeTable(line);
				}
			} else if ("SLog".equals(curTable)) {
				Matcher m=field.matcher(line);
				if (m.matches()) {
					String key=m.group(1).toLowerCase();
					String value=Literal.fromLiteral(m.group(2));
					if ("id".equals(key)) {
						flush();
						curlog=LogRecord.create(Integer.parseInt(value),sdb);
					}
					if ("option".equals(key)) {
						curlog.option=value;
					}
					if ("target".equals(key)) {
						curlog.target=value;
					}
					if ("action".equals(key)) {
						curlog.action=value;
					}
					if ("date".equals(key)) {
						curlog.date=value;
					}
				} else {
					tryChangeTable(line);
				}
			}
		} 
		flush();
		s.close();
	}
	private DocumentRecord getdoc(String value) {
		DocumentRecord res = sdb.byId(value);
		if (res!=null) return res;
		return sdb.newDocument(value);
	}
	private void tryChangeTable(String line) {
		Matcher m2=table.matcher(line);
		if (m2.matches()) {
			curTable=m2.group(1);
			flush();
		}
	}
	private void flush() {
		if (curlog!=null) {
			sdb.importLog(curlog);
			curlog=null;
		}
		if (curdoc!=null) {
			sdb.save(curdoc, new HashMap<String, String>());// curdoc.save();
			if (documentLoader!=null) {
				documentLoader.reload(curdoc.id);
			}
			curdoc=null;
		}
	}
	public Importer(SDB documentSet) {
		super();
		this.sdb = documentSet;
		documentLoader=null;
	}
	public Importer(DocumentLoader documentLoader) {
		this.documentLoader=documentLoader;
		sdb=(SDB) documentLoader.getDocumentSet();
	}
	
}
