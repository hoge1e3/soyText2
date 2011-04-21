package jp.tonyu.soytext2.search;

import jp.tonyu.debug.Log;


public class QueryResult {
 	public final boolean filterMatched, templateMatched;
	// matched                  T                T   <-  name：hoge    に  "name:?", {name:hoge} をマッチ
	// templateMatchable        F                T   <-  name:fuga    に "name:?", {name:hoge} をマッチ
	// notMatched;              F                F   <-  nameがないDoc に  "name:?"  をマッチ
 	//                          T                F   (とりあえず禁止  "-name:?" )

 	public QueryResult(boolean filterMatched,
 			boolean templateMatched) {
 		super();
 		this.filterMatched = filterMatched;
 		this.templateMatched = templateMatched;
 		if (filterMatched && !templateMatched) Log.die("This combination not allowed");
 	}
 	public QueryResult(boolean filterMatched) {
 		this(filterMatched,filterMatched); 
 	}
					
}

