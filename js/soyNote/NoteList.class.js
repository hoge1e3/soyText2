var SoyText;
var NoteList=Class.create({  
	onFindTextKeydown:function ( e ) {
        if  ( e .keyCode!= 13 )  return ;
        this.search();
	},
	search:function () { 
        var  it = this .items( ) ; 
        var  t = this .findText( ) ; 
        it .empty( ) ; 
        SoyText .search( t .val( ) ) .each( function  ( d )  {
           it.append( hprintf ( 
             "<span class=NoteItem page=%a>%h</span><br/>" , 
             d .id, d .summary) 
           ); 
        }).next( function  ( )  {
           debug ( "END Search" ) ; 
        }); 
	},
	onNewNoteClick:function () {
		var n=prompt("Name");
		var t=this;
		if (n) {
			SoyText.create({name:n,body:sprintf("// %s ",n)}).next(function (d) {
				NoteApplication.closestFrom(t).editor().open(d);
			}).error(printError.onError);
		}
	},
	cssName:function ( ) {var _THIS_=this; return  "NoteList" ;  },
  	subPartsNames:function ( ) {var _THIS_=this; return  "items,findText,newNote" ;  }
});