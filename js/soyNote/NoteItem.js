NoteItem=Class.create({
  initialize: function (jq) {
     this.page=SoyText.byId(jq.attr("page" ));  
  }, 
  cssName: function () { return "NoteItem" ; },
  noteApplication: function (){
    return NoteApplication.closestFrom(this);
  },
  editor: function () {
     return this.noteApplication().editor();
  },
  open: function () {
    this.editor().open(this.page);
  //debug(page.summary);
  },
  onClick: function () {
    debug("ClICK!" );
    this.open();
  }
});