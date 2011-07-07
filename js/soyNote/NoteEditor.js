NoteEditor=Class.create({
  open: function (page) {
    debug("Edit - open=" +page.id);
    var b=this.body();
    if (this.tx) this.tx.close();
    this.tx=new TextEditor2(page, {uis:{body: b}} );
  },
  cssName: function () { return "NoteEditor" ; },
  subPartsNames: function (){return "body,attr" ;}
});