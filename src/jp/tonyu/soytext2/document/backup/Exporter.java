package jp.tonyu.soytext2.document.backup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import jp.tonyu.soytext2.document.DocumentAction;
import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.document.LogAction;
import jp.tonyu.soytext2.document.SDB;
import jp.tonyu.soytext2.document.LogRecord;
import jp.tonyu.util.Literal;

public class Exporter {
	public static void printNonNull(PrintStream p,String field, Object value) {
		if (value!=null) p.printf("%s: %s\n", field, Literal.toLiteralPreserveCR(value+""));
	}
	public Exporter(SDB db, File out) throws FileNotFoundException {
		final PrintStream p=new PrintStream(out);
		p.println("[Document]");
		db.all(new DocumentAction() {
			
			@Override
			public boolean run(DocumentRecord d) {
				printNonNull(p,"id", d.id);
				printNonNull(p,"createdate", d.createDate);
				printNonNull(p,"lastaccessed", d.lastAccessed);
				printNonNull(p,"lastupdate", d.lastUpdate);
				printNonNull(p, "summary", d.summary);
				printNonNull(p, "precontent", d.preContent);
				printNonNull(p, "content", d.content);
				printNonNull(p, "owner", d.owner);
				printNonNull(p, "group", d.group);
				printNonNull(p, "permission", d.permission);
				return false;
			}
		},false);
		p.println("[SLog]");
		db.all(new LogAction() {
			
			@Override
			public boolean run(LogRecord log) {
				printNonNull(p,"id", log.id);
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
