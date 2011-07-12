var JSLINT,debug,TextEditor2,hprintf;
var NoteEditor=Class.create({
  open: function (page) {
    debug("Edit - open=" +page.id);
    var b=this.body();
    var s=this.modifiedStar();
    if (this.tx) { this.tx.close(); }
    this.tx=new TextEditor2(page, {uis:{body: b, modified:s}} );
  },
  cssName: function () { return "NoteEditor" ; },
  subPartsNames: function (){return "body,attr,modifiedStar,lint,errorPanel" ;},
  onLintClick: function () {
  	var r=JSLINT(this.getText(), {white:true});
    if (!r) {
       var a=JSLINT.errors;
       var e=this.errorPanel();
       e.empty();
       for (var i=0 ; i<a.length ;i++) {
       	  e.append(hprintf("<div>%h</div>",a[i].reason));
       } 
    }
  },
  getText : function () {
  	if (this.tx) { return this.tx.getText(); }
  	return "";
  }
});