package jp.tonyu.soytext2.document.importing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import jp.tonyu.soytext2.document.Document;
import jp.tonyu.soytext2.document.DocumentSet;
import jp.tonyu.util.SFile;


public class Importer {
	final DocumentSet documentSet;
	public void importDocuments(File file) throws IOException {
		Scanner s=new Scanner(file);
		while (s.hasNextLine()) {
			String id=s.nextLine();
			if (id==null) break;
			String sum=s.nextLine();
			if (sum==null) break;
			String content=s.nextLine();
			if (content==null) break;
			Document d=documentSet.newDocument(id);
			d.content=content;
			d.summary=sum;
			d.save();
		} 
		s.close();
	}
	public Importer(DocumentSet documentSet) {
		super();
		this.documentSet = documentSet;
	}
	
}
