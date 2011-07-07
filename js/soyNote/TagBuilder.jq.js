TagBuilderJ=Class.create({
    initialize: function () {
       this.target=null;
    },
    add:function (a) {
	  var target=this.target;
	  var t=typeof(a);
	  if (t=="string" || t=="number" || t=="boolean") {
	     return this.addStr(a);
          }
	  if (a instanceof Array) return this.addArray(a);
	  if (a instanceof Function) return a(target,this);
	  return this.addObj(a);  
	},
    // private method
    addStr: function (s) {
        var target=this.target;
        if (target==null) alert("target is null");
        target.append(htmlEscape(""+s)); 
        return this;
    },
    // private method
    addArray: function (a,idx) {
	  var target=this.target;
	  if (typeof(a[0])=="string") {
	    var tagName=a[0];
	    var ref;
	    if (tagName.match(/(\w+)#(.+)/)) {
	       tagName=RegExp.$1;
	       ref=RegExp.$2;
	    }
	    tagName=tagName.toLowerCase();
	    var oldTarget=target;
            this.target=TagBuilderJ.$("<"+tagName+">");
            if (this.jqObject==null) this.jqObject=this.target;
            if (this.refObject==null) this.refObject=this.target;
            if (this.refObject==null) debug("Null "+tagName);
            this.setRef(ref,this.target);
            for (var i=1 ; i<a.length ; i++) {
	       this.add(a[i]);
	    }
            if (oldTarget!=null) {
               oldTarget.append(this.target);
            } 
            this.target=oldTarget;            
	  } else {
  	    for (var i=0 ; i<a.length ; i++) {
	      this.add(a[i]);
	    } 
          } 
	  return this;
	},
	// private method
	setRef: function (name, value) {
	  if (this.refObject && name) {
	     //_debug("Set ref "+(this.refObject instanceof TagBuilder)+"."+name);
	      this.refObject[name]=value;
	  } 
	},
        referenceObject: function(r) {
           if (arguments.length==0) return this.refObject;
           var old=this.refObject;
           this.refObject=r;
           return old;
        },
	// private method
	addObj:	function (o,idx) {
          var target=this.target;
          if (target==null) alert("Taget is null"); 
          if (o==null) return;
          if (o.jquery!=null) {
             target.append(o);
             return;
          }
	  for (var i in o) {
	     var v=o[i];
             if (i=="style") {
		    this.setStyle(v);
	     } else if (i.match(/^on/) && typeof(v)=="function") {
		    i=i.toLowerCase();
                    if (i=="onenter") {
		       i="onkeydown";
		       var pv=v;
		       v=function ent(e) {
                          if (e.keyCode==13) pv();
		       };
                    }
		    /*if (this.tagNameIs("a") && i=="onclick" &&
                        !target.href) {
                        target.attr("href","javascript:;");
                    }*/
                    var evt=i.replace(/^on/,"");
                    target[evt].apply(target,[v]);
	     } else {
	       target.attr(i,v);
	     }
	  }
	  return this;
	},
        setStyle:function (s) {
           var target=this.target;
           if (target==null) alert("Target is null");
           target.css(s);
        }
});
TagBuilderJ.$=jQuery;//.noConflict();
// private function
String.prototype.eqi=function (to) {
    return this.toLowerCase()==to.toString().toLowerCase();
};
// private function
function checkTagName(a,b) {
     ((b instanceof RegExp && b.match(a)) || a.eqi(b)) 
	 || alert("<"+a+">!=<"+b+">");
}

// private function
function isHTMLElement(e) {
   return typeof(e)=="object" && e.tagName;
}
// private function
function checkNull(v,msg){
   if (v==null) alert(msg+" is null");
   return v;
}
htmlEscape=(function  (){
  var map = {"<":"&lt;", ">":"&gt;", "&":"&amp;", "'":"&#39;", "\"":"&quot;", " ":"&nbsp;"};
  var replaceStr = function(s){ return map[s]; };
  return function(str) { return str.replace(/<|>|&|'|"| /g, replaceStr); };
})();
function $t(expr,ref) {
   if (expr.jquery!=null) return expr;
   var t=new TagBuilderJ();
   if (ref!=null) t.referenceObject(ref);
   t.add(expr);
   if (t.refObject==null) {debug("Null!");debug(expr);}
   return t.refObject;
}