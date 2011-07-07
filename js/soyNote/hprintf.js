hprintf=function () {
  var a=$A(arguments);
  var fmt=a.shift();
  return fmt.replace(/%./g,function () {
     var type=RegExp.lastMatch;
     if (type=="%a") return "\""+a.shift()+"\"";
     if (type=="%h") return a.shift();
     return type.substring(1);
  });
};