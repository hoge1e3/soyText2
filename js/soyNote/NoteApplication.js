var NoteApplication=Class.create({
	cssName:function () {return  "NoteApplication" ; },
    editor:function () {return  NoteEditor.findFrom( this ) ; }
}); 