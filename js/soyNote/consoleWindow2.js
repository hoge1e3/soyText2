var console, $break;
/*consoleWindow=function() {
  var f=new Frame("Console", ["textarea#txt",{rows:10, cols:40}] );
  function print(line) {
     f.txt.get(0).value+=line+"\n";
  } 
  return {print:print, frame:f}; 
};
_con=null;*/
var debug=function (x) {
  /*if (_con==1) return;
  if (_con==null) {
     _con=1;
     _con=consoleWindow();
     _con.frame.onClose=function () {_con=null;}
  }
  _con.print(x);*/
  console.log(x);
}; 
var printError=function (t) {
 return function () {
   try {
      if (typeof t!="function") {return;}
      return t.apply(this,arguments);
   }catch(e) {
      if ($break!==null && e!==$break) {
        printError.onError(e);
      }
      throw e;
   }
 };
};
printError.onError=function (e) {
	try {
		console.trace(e);
	    new TraceDisplay2(e);
	} catch (ex) {
		console.trace(ex);
	}
};
Function.prototype.pErrf=function () {
  return printError(this);
};