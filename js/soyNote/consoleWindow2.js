consoleWindow=function() {
  var f=new Frame("Console", ["textarea#txt",{rows:10, cols:40}] );
  function print(line) {
     f.txt.get(0).value+=line+"\n";
  } 
  return {print:print, frame:f}; 
};
_con=null;
debug=function (x) {
  if (_con==1) return;
  if (_con==null) {
     _con=1;
     _con=consoleWindow();
     _con.frame.onClose=function () {_con=null;}
  }
  _con.print(x);
};
printError=function (t) {
 return function () {
   try {
      if (t==null) return;
      return t.apply(this,arguments);
   }catch(e) {
      if ($break!=null && e!==$break) {
        printError.onError(e);
      }
      throw e;
   }
 };
}
printError.onError=function (e) {
    new TraceDisplay2(e);
};
Function.prototype.pErrf=function () {
  return printError(this);
};