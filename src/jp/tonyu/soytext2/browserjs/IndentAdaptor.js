var _top={__ie:navigator.userAgent.indexOf("MSIE") != -1};

function $conv(id) {
   if (typeof(id)=="string") return document.getElementById(id);
   return id;
}
function addEventListener(elem,type,func) {
   if (_top.__ie) {
      elem.attachEvent("on"+type,func);
   } else {
      elem.addEventListener(type,func,false);
   }
}

function attachIndentAdaptor(elem) {
    elem=$conv(elem);
	var nextInd=false;
	var indDepth="";
    addEventListener(elem,"keydown",function (e) {
      //var t=document.getElementById("a");
	  //document. addEventlistener("keydown",t);
	  if (e.keyCode==13){
	     nextInd=true;
		 var pos=getCaretPos(elem);
   	     var t=elem;
		 pos--;
	     while (pos>0) {
		    if (t.value.charCodeAt(pos)==10) break;
			pos--;
		 }
		 pos++;
		 var len=t.value.length;
		 indDepth="";
		 while(pos<len) {
		    if (t.value.charCodeAt(pos)!=32) break;
			pos++;
			indDepth+=" ";
		 }
		 if (!_top.__ie) {
		    e.preventDefault(); 
	        e.stopPropagation(); 
			setSelText(elem, "\n"+indDepth);
			nextInd=false;
		 } else {
		    e.returnValue = false; 
	        e.cancelBubble = true; 
			setSelText(elem, "\n"+indDepth);
			nextInd=false;
		 }
	  }
   });
   addEventListener(elem, "keyup" ,function (e) {
      if (nextInd) {
	      //document.title=indDepth.length;
          setSelText(elem, indDepth);
		  nextInd=false;
	  }
   });
}
var attachIndentAdapter=attachIndentAdaptor;

function getCaretPos(elem){
  var __self=this;
  if (_top.__ie) {
    var s = elem;
    if( document.selection ){
      var range = document.selection.createRange();
      var stored_range = range.duplicate();
      stored_range.moveToElementText( s );
      stored_range.setEndPoint( 'EndToEnd', range );
      var start  = stored_range.text.length - range.text.length;
      var length = range.text.length;
      return start;
    }
  } else {
    var s=elem;
    if (s==undefined) alert("atextrange getcaretpos not found "+id);
    //s.setSelectionRange(s.value.length,s.value.length);
    this.start=s.selectionStart;
    this.length=s.selectionEnd-s.selectionStart;
    return this.start;
  }
}
;
function setSelText(elem, text){
  if (_top.__ie) {
    var r = document.selection.createRange();
    r.text=text;
  } else {
    var s=elem;
    var scrollPos = s.scrollTop; 
    var cont=s.value;
    var b=cont.substring (0,s.selectionStart)+text;
    s.value=b+cont.substring (s.selectionEnd);
    s.setSelectionRange(b.length,b.length);
    s.scrollTop=scrollPos; 
    //alert("["+cont+"]");
  }
} 


function setRange(elem, from, to) {
  var s=elem;
  if (s==undefined) throw id+" not found";
  if (_top.__ie) {
    var r=s.createTextRange();
	//alert(r);
    r.move("character",from);
    r.select();
  } else {
    s.setSelectionRange(from,to);
    s.focus(); //select();
	//s.scrollTop=s.scrollHeight/2;
  } 
}