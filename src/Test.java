import java.io.File;

import org.tmatesoft.sqljet.core.SqlJetException;

import jp.tonyu.soytext2.db.SDB;
import jp.tonyu.soytext2.document.Document;
import jp.tonyu.soytext2.document.DocumentAction;


public class Test {
	public static void main(String[] args) throws SqlJetException {
		SDB s=new SDB(new File("test.db"));
		Document d=s.newDocument();
		d.content="test"+d.id;
		s.save(d);
		s.all(new DocumentAction() {
			
			@Override
			public boolean run(Document d) {
				System.out.println(d);
				return false;
			}
		});
		s.close();
	}
}
