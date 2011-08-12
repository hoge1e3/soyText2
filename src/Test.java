import java.io.File;

import org.tmatesoft.sqljet.core.SqlJetException;

import jp.tonyu.soytext.Origin;
import jp.tonyu.soytext2.document.DocumentRecord;
import jp.tonyu.soytext2.document.DocumentAction;
import jp.tonyu.soytext2.document.SDB;


public class Test {
	public static void main(String[] args) throws SqlJetException {
		final SDB s=new SDB(new File("test.db"), Origin.uid);
		/*Document d=s.newDocument();
		d.content="test"+d.id;
		s.save(d);*/
		class Work extends Thread {
			public void run() {
				s.all(new DocumentAction() {
					
					@Override
					public boolean run(DocumentRecord d) {
						System.out.println(d.id+" "+d.lastUpdate+" "+d.content);
						try {
							Thread.sleep(0);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						return false;
					}
				});				
			}
			
		};
		Work t1=new Work(),t2=new Work();
		t1.start();
		try {
			Thread.sleep(0);
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
		
		s.printLog();
		
		s.close();
	}
}
