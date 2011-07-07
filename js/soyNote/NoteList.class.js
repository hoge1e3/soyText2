NoteList=Class.create({  onFindTextKeydown:function ( e ) {var _THIS_=this;
        if  ( e .keyCode!= 13 )  return ; 
        var  it = this .items( ) ; 
        var  t = this .findText( ) ; 
        it .empty( ) ; 
        SoyText .search( t .val( ) ) .each( function  ( d )  {
           it .append( hprintf ( 
             "<span class=NoteItem page=%a>%h</span><br/>" , 
             d .id, d .summary) 
           ) ; 
        }) .next( function  ( )  {
           debug ( "END Search" ) ; 
        }) ; 
},

  cssName:function ( ) {var _THIS_=this; return  "NoteList" ;  },
  subPartsNames:function ( ) {var _THIS_=this; return  "items,findText" ;  },__dummy:false});