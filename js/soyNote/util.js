function toURLParams(obj) {
   var res=[];
   for (var key in obj) {
      if (key!="id") {
         res.push(key+"="+encodeURIComponent(obj[key]));
      }
   }
   return res.join("&");
}
function notNull(val,msg) {
   if (val==null) throw msg;
   return val;
}
/*function require(id) {
   byId(id,ev);
   function ev(o) {
      eval(o._body);
   }
}*/

function overRide(obj,name,func) {
   if (obj.base==null) obj._super={};
   obj.base[name]=obj[name];
   obj[name]=func;
}
function procAjax(p) {
   if (p==null) return NOP;
   if (printError) p=printError(p);
   return function (res) {
       p(res.responseText,res);
   };
}
function trace(s) {
  if (consoleArea) {consoleArea.target.value+=s+"\n";}
}
function pos(x,y) {
	return {position:"absolute", left:x, top:y};
}
function posDiv(x,y,cont) {
	return ["div",{style:pos(x,y)},cont];
}
function toLines(s) {
    if (s instanceof Array) return s;
    return s.split(/\r?\n/);
}
function NOP(){};


ieSplit=function(text,sreg){
        var reg=(typeof sreg=='string')?RegExp(sreg,'g'):RegExp(sreg.source,'g'+((sreg.ignoreCase)?'i':'')+((sreg.multiline)?'m':''));
        if (!reg.source) return [text];
        var sindex=0, eindex, elms=[];
        do {
            reg.exec(text);
            eindex=reg.lastIndex;
            elms.push(text.substring(sindex,(eindex)?eindex:text.length).replace(sreg,''));
            sindex=eindex;
        } while (sindex);
        return elms;
};
if (navigator.userAgent.match(/msie/i)) {
   toLines=function (s) {
      if (s instanceof Array) return s;
      return ieSplit(s,/\r?\n/);
   };
   //    String.prototype.split=ieSplit;
}
