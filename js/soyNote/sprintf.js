sprintf=function () {
  var a=$A(arguments);
  var fmt=a.shift();
  return fmt.replace(/%./g,function () {
     var type=RegExp.lastMatch;
     if (type=="%d") return a.shift();
     if (type=="%s") return a.shift();
     return type.substring(1);
  });
};