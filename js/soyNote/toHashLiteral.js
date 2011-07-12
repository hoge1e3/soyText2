toHashLiteral=function (hash) {
 return _self(hash);
 function _self(hash) {
   var res="{";
   var com="";
   for (var key in hash) {
     if (!hash.hasOwnProperty(key)) continue;
     var value=hash[key];
     res+=com; com=",";
     res+="\""+key+"\": "+lit(value);
   }
   return res+"}";
 }
   function lit(value) {
     if (value && value.id) {
        return document(value);
     } else if (typeof value=="number") {
        return value;
     } else if (typeof value=="function") {
        return value+"";
     } else if (typeof value=="boolean") {
        return value+"";
     } else if (typeof value=="string") {
        return str(value);
     } else if (typeof value=="object") {
        if (value instanceof Array) {
           return ary(value);
        } else {
           return _self(value);
        }
     } else if (value==null) {
        return "null";
     } else {
        return "NOT COMVERT"+value;
     }  
   }
   function document(d) {
     return "$.byId(\""+d.id+"\")";
   }
   function str(s) {
     s=s.replace(/\\/g,"\\\\")
        .replace(/\n/g,"\\n")
        .replace(/\r/g,"\\r")
        .replace(/\"/g,"\\\"");
     return "\""+s+"\"";
   }
   function ary(s) {
     return "["+s.map(lit).join(", ")+"]";
   }
};