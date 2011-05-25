package jp.tonyu.soytext2.document.backup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import jp.tonyu.soytext2.document.DocumentAction;
import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.document.LogAction;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.document.LogRecord;

public class Exporter {
	public static void printNonNull(PrintStream p,String field, String value) {
		if (value!=null) p.printf("%s: %s\n", field, value);
	}
	public Exporter(SDB db, File out) throws FileNotFoundException {
		final PrintStream p=new PrintStream(out);
		p.println("[Document]");
		db.all(new DocumentAction() {
			
			@Override
			public boolean run(DocumentRecord d) {
				p.printf("id: %s\n", d.id);
				p.printf("createdate: %d\n", d.createDate);
				p.printf("lastaccessed: %d\n", d.lastAccessed);
				p.printf("lastupdate: %d\n", d.lastUpdate);
				printNonNull(p, "summary", d.summary);
				printNonNull(p, "precontent", d.preContent);
				printNonNull(p, "content", d.content);
				printNonNull(p, "owner", d.owner.get());
				printNonNull(p, "group", d.group.get());
				printNonNull(p, "permission", d.permission.get());
				return false;
			}
		},false);
		p.println("[SLog]");
		db.all(new LogAction() {
			
			@Override
			public boolean run(LogRecord log) {
				p.printf("id: %d\n", log.id);
				printNonNull(p,	"date", log.date);
				printNonNull(p,	"action", log.action);
				printNonNull(p,	"target", log.target);
				printNonNull(p,	"option", log.option);
				return false;
			}
		});
		p.close();
	}
}
