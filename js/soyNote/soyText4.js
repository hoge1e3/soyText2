var SoyText={};
SoyText.cache={};
SoyText.byId=function (id) {
  if (SoyText.cache[id]) { return SoyText.cache[id]; }
  return SoyText.cache[id]=new DocumentScriptable(id);  
};
SoyText.search=function (cond, vars) {
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
            d.lastUpdate=lastUpdate;
            loop(d);
         }
       }
     }
  }
};
SoyText.save=function (hash) {

};
SoyText.extend=Object.extend;