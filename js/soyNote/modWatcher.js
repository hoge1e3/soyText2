var modWatcher=function (handlers) {
  function watch() {
     if (handlers.getModelContent()!=handlers.getEditorContent()) {
        handlers.onModified();
        w.state(NOP);
        setTimeout(_save.pErrf(),500);
     }
  }
  function _save() {
     handlers.onSave.pErrf()(handlers.getEditorContent(), resume);
  }
  function resume () { w.state(watch); }
  function stop() {w.die();}
// onModified(), onSave(editorContent, next), 
// getModelContent():str, getEditorContent():str
  if (handlers.getModelContent()!=handlers.getEditorContent()) {
      alert("modWatcher: ensure modelContent==editorContent on startup");
      return;
  } 
  var w=fiber(printError(watch));
  return {stop:stop};
}