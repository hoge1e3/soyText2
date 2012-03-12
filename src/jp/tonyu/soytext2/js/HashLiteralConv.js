SoyText={};
SoyText.generateContent=function (d) {
	var buf="";
	var ctx={
	   d2sym:{},
	   indentC:0,
	   indentBr: function () {
		 this.indentC++;
		 return this.br();
	   },
	   dedentBr: function (){
		   this.indentC--;
		   return this.br();
	   },
	   br: function () {
		   return "\n"+this.curIndent();
	   },
	   curIndent:function () {
		   return rept("    ",this.indentC);
	   }
	};
	if (d.scope) {
		for (var k in d.scope) {
			var value=d.scope[k];
			buf+="var "+k+"="+expr(value ,ctx)+"\n";
			var id=isDocument(value);
			if (id) ctx.d2sym[id]=k;
		}
	}
	buf+="$.extend(_,"+hash(d,ctx)+");"
	return buf;
	
	function expr(value,ctx) {
		if (isDocument(value)) {
	        return document(value,ctx);
	     } else if (typeof value=="number") {
	        return value;
	     } else if (typeof value=="boolean") {
	        return value+"";
	     } else if (typeof value=="function") {
	    	 return func(value,ctx);
	     } else if (typeof value=="string") {
	        return str(value,ctx);
	     } else if (value==null) {
	        return "null";
	     } else if (typeof value=="object") {
	        if (value instanceof Array) {
	           return ary(value,ctx);
	        } else {
	           return hash(value,ctx);
	        }
	     } else {
	        return "null";
	     }  
	}
	function func(f,ctx) {
   	     f=SoyText.decompile(f,ctx.indentC*4);
   	     //f=f+"";
	     f=f.replace(/\r/g,"").replace(/^\n/,"").replace(/^\s*/,"").replace(/\n$/,"");
	     return f;
	     
	     /*var fa= f.split(/\n/);
	     var res=fa.map(function (line, no) {
	    	 if (true) return line;
	    	 return ctx.curIndent()+line;
	     });
	     res=res.join("\n");
	     return res;*/
	     /*f=f.replace(/\}$/,ctx.curIndent()+"}");
	     if (f.substring(f.length-1, f.length)=="\n") {
	    	 f=f.substring(0, f.length-1);
	     }*/
	}
	function hash(h,ctx) {
		var blessed;
		if (isDocument(h.constructor)) { //} || typeof(h.constructor)=="function") {  in what case?
			                             // It is comment out due to h.construcotr==Object or Array or what else
			blessed=h.constructor;
		}
	   var res=(blessed?"$.bless("+expr(blessed,ctx)+",":"")+"{"+ctx.indentBr();
	   var kv=[];
	   for (var key in h) {
		   if (h.hasOwnProperty && !h.hasOwnProperty(key)) continue;
		   if (blessed && key=="constructor") continue;
		   var value=h[key];
		   kv.push([key,value]);
	   }
	   kv.each(function (e,idx) {
		  var key=e[0];
		  var value=e[1];
		  var valueStr=expr(value,ctx);
		  res+=hashKey(key+"")+": "+valueStr;
		  if (idx<kv.length-1) {
			  res+=","+ctx.br();
		  }
	   });
       return res+ctx.dedentBr()+"}"+(blessed?")":"");
	}
	function isDocument(d) {
		return SoyText.isDocument(d);
	}
    function str(s) {
	     s=s.replace(/\\/g,"\\\\")
	        .replace(/\n/g,"\\n")
	        .replace(/\r/g,"\\r")
	        .replace(/\"/g,"\\\"");
	     return "\""+s+"\"";
	}
	function ary(s,ctx) {
	    var res="["+ctx.indentBr();
	    res+=s.map(function (e) { return expr(e,ctx); }).join(", "+ctx.br());
	    res+=ctx.dedentBr()+"]";
	    return res;
	}
	function document(d,ctx) {
		 var name=ctx.d2sym[d.id];
		 if (name) {
			 return name;
		 } else {
			 return "$.byId("+str(d.id)+")";
		 }
    }
    function rept(str,times) {
    	var res="";
    	for (;times>0;times--) res+=str;
    	return res;
    }
    function isSymbol(s) {
    	return s.match(/^[a-zA-Z_\$][\w\d\$]*$/);
    }
    function hashKey(s) {
    	if (isSymbol(s)) return s;
    	else return str(s);
    }
}; // tohash

