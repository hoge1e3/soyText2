var DocumentScriptable;
var SoyText={
  cache:{},
  byId:function (id) {
	  if (SoyText.cache[id]) { return SoyText.cache[id]; }
	  return SoyText.cache[id]=new DocumentScriptable(id);  
  },
  search:function (cond, vars) {
	  return {each:startSearch};
	  function startSearch(loop) { 
	     return $.get( SoyText.rootPath()+"/search",
	       {q:cond,_responseType:"ajax"}).next(comp);
	        
	     function comp(r) {
		       var a=toLines(r);
		       var state=body;
		       a.each(p);
		       function p(line) {
		         state(line);
		       }
		       function body(line){
		         var fld=line.split(/\t/);
		         var lastUpdate=fld[0];
		         var id=fld[1];
		         var summary=fld[2];
		         //debug(line+"   "+id+"summary="+summary);
		         if (id && id.length>0) {
		            var d=SoyText.byId(id);
		            d.summary=summary;
		            if (typeof d.summary!="string" || d.summary.length==0) {
		            	d.summary=id;
		            }
		            d.lastUpdate=lastUpdate;
		            loop(d);
		         }
		       }
	     }
	  }
	},
   generateContent:function (hash) {
	 var  s = toHashLiteral ( hash ) ; 
	 return sprintf("$.extend(_,%s);",s); 
   },
   create:function (hash) {
	  var  url = sprintf ( "%s/new" , SoyText .rootPath( )) ; 
	  return $.post(url,{content: SoyText.generateContent(hash)}).next(function (r) {
	  		if (r.match(/<!--AJAXTAG:id:([^\-]+)-->/)) {
	  			var id=RegExp.$1;
	  			var d=SoyText.byId(id);	
	  			return d.load();
	  		} else {alert(r);}  		
	  });
	},
	extend:Object.extend
};