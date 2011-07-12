var main=function () {
  NoteApplication.bindCSS();
  NoteEditor.bindCSS();
  NoteItem.bindCSS(); 
  NoteList.bindCSS();
  NoteList.findFrom($("body")).search();
};