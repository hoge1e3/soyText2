import java.io.File;

import org.tmatesoft.sqljet.core.SqlJetException;

import jp.tonyu.soytext2.db.SDB;
import jp.tonyu.soytext2.document.Document;
import jp.tonyu.soytext2.document.DocumentAction;


public class Test {
	public static void main(String[] args) throws SqlJetException {
		final SDB s=new SDB(new File("test.db"));
		Document d=s.newDocument();
		d.content="test"+d.id;
		s.save(d);
		class Work extends Thread {
			public void run() {
				try {
					s.all(new DocumentAction() {
						
						@Override
						public boolean run(Document d) {
							System.out.println(d);
							System.out.println(d.content);
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							return false;
						}
					});
				} catch (SqlJetException e) {
					e.printStackTrace();
				}				
			}
			
		};
		Work t1=new Work(),t2=new Work();
		t1.start();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		t2.start();
		
		try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		s.close();
	}
}
