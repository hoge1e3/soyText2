package jp.tonyu.soytext2.document;

import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import jp.tonyu.debug.Log;

public class IntersectDocumentRecordIterator implements DocumentRecordIterator{
	List<DocumentRecordIterator> iters=new Vector<DocumentRecordIterator>();
	DocumentRecord next;
	public void add(DocumentRecordIterator it) {
		iters.add(it);
	}
	@Override
	public boolean hasNext() throws SQLException {
		//DocumentRecord[] lasts=new DocumentRecord[iters.size()];
		DocumentRecord oldest=null;
		//boolean allNoNext=false;
		while (true) {
			boolean succ=true;
			for (DocumentRecordIterator it:iters) {
				if (!it.hasNext()) {
					//Log.d("intersect", it+" has no more elements");
					return false;
				}
				DocumentRecord cur=it.next();
				//Log.d("intersect", "Get from "+it+" cur="+cur+" oldest="+oldest+" cur.lu="+cur.lastUpdate);
				if (oldest==null) {
					oldest=cur;
				} else {
					while (cur.lastUpdate>oldest.lastUpdate) {
						if (!it.hasNext()) {
							//Log.d("intersect", it+" has no more elements2");
							return false;
						}
						cur=it.next();
						//Log.d("intersect", "Get from2 "+it+" cur="+cur+" oldest="+oldest+" cur.lu="+cur.lastUpdate);
					}
					if (cur.lastUpdate<oldest.lastUpdate) {
						//Log.d("intersect", "Retry: "+cur+" is older than "+oldest);
						oldest=cur;
						succ=false;
						break;
					}
				}
			}
			if (succ) {
				next=oldest;
				//Log.d("intersect", " succ next="+oldest);
				return next!=null;
			}
		}
	}

	@Override
	public DocumentRecord next() throws SQLException {
		if (next==null && !hasNext()) Log.die("Next is null");
		DocumentRecord res = next;
		next=null;
		return res;
	}

	@Override
	public void close() throws SQLException {
		for (DocumentRecordIterator it:iters) {
			it.close();
		}
	}

}
