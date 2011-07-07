TextEditor2=Class.create({ 
 
  
 
 
 
 
  initialize:function ( _page  ,options ) {var _THIS_=this;
   if  ( arguments .length== 2  ||  _page  instanceof  DocumentScriptable )  {
     (_THIS_.page)= _page ; 
     (_THIS_.start.bind(_THIS_))( options ) ; 
   } else  {
     (_THIS_.start.bind(_THIS_))( _page ) ; 
   }
},
  start:function ( options ) {var _THIS_=this;
   Object .extend( this , options ) ; 
   if  ( ! ( (_THIS_.page).id)  )  {
     if  ( (_THIS_.page).body== null )  (_THIS_.page).body= "" ;     
     if  ( (_THIS_.onNewDocument))  (_THIS_.onNewDocument)( (_THIS_.page)) ; 
     (_THIS_.onPageReady.bind(_THIS_))( ) ; 
   } else  {
     (_THIS_.page).load( (_THIS_.onPageReady.bind(_THIS_))) ;    
   }
},
  onPageReady:function ( ) {var _THIS_=this;
   if  ( (_THIS_.uis).body)  {
     var  tx = (_THIS_.uis).body.get( 0 ) ; 
     var  ind = "indentAdaptered" ; 
     if  ( ! jQuery .data( tx , ind ) )  {
        jQuery .data( tx , ind , true ) ; 
        attachIndentAdapter ( tx ) ; 
     }
     (_THIS_.uis).body.val( (_THIS_.page).body) ; 
   }
   (_THIS_.setNameBox.bind(_THIS_))( ) ; 
   if  ( (_THIS_.uis).name)  {
     (_THIS_.uis).name.change( function  ( )  {
        if  ( (_THIS_.setName.bind(_THIS_))(  (_THIS_.appendExt.bind(_THIS_))( (_THIS_.uis).name.val( ) )  )  )  {
            (_THIS_.save.bind(_THIS_))( ) ; 
        } else  {
            (_THIS_.setNameBox.bind(_THIS_))( ) ; 
        }
     }) ; 
   }
   (_THIS_.mdw)= modWatcher ( { 
      onModified : function ( ) {(_THIS_.setModified.bind(_THIS_))( true ) ; },  
      "onSave" :  function  ( cont , next )  {
         (_THIS_.page).body= cont ; 
         if  ( ! (_THIS_.manualSave))  (_THIS_.page).save( (_THIS_.saveCompleteAction.bind(_THIS_))( next ) ) ; 
         else  {next ( ) ; }
      },  
      getModelContent : function ( ) {return  (_THIS_.page).body; },  
      getEditorContent : function ( ) {
          if  ( (_THIS_.uis).body)  {
             return  (_THIS_.uis).body.val( ) ; 
          } else  {
             return  "" ; 
          }
      }
   }) ; 
},
  setNameBox:function ( ) {var _THIS_=this;
   var  n = (_THIS_.getName.bind(_THIS_))( ) ; 
   if  ( n  &&  (_THIS_.uis).name)  {
      (_THIS_.uis).name.val( (_THIS_.stripExt.bind(_THIS_))( n ) ) ; 
   }
},
  stripExt:function ( n ) {var _THIS_=this;
   if  ( n .endsWith( (_THIS_.extension)) )  {
      return  n .substring( 0 , n .length- (_THIS_.extension).length) ; 
   }
   return  n ; 
},
  appendExt:function ( n ) {var _THIS_=this;
   if  ( n .endsWith( (_THIS_.extension)) )  {
      return  n ; 
   }
   return  n + (_THIS_.extension); 
},
  close:function ( ) {var _THIS_=this;
   (_THIS_.mdw).stop( ) ;  
},
  setModified:function ( s ) {var _THIS_=this;
   if (  (_THIS_.uis).modified)  (_THIS_.uis).modified.text( s ? "*" : "" ) ; 
},
  save:function ( next ) {var _THIS_=this;
    (_THIS_.setModified.bind(_THIS_))( true ) ; 
    if  ( ! next )  next = NOP ;   
    (_THIS_.page).save( (_THIS_.saveCompleteAction.bind(_THIS_))( next ) ) ; 
},
  saveCompleteAction:function ( next ) {var _THIS_=this;
   var  t = this ; 
   return  printError ( function  ( )  {
      if ( (_THIS_.page).name)  {
         ok2 ( ) ; 
      } else  {
         (_THIS_.setName.bind(_THIS_))( (_THIS_.appendExt.bind(_THIS_))( "無題_" + (_THIS_.page).id) ) ;  
         (_THIS_.setNameBox.bind(_THIS_))( ) ;  
         (_THIS_.page).save( ok2 ) ;         
      }
      function  ok2 ( )  {
         (_THIS_.setModified.bind(_THIS_))( false ) ; 
         if  ( (_THIS_.onSave))  (_THIS_.onSave)( (_THIS_.page)) ; 
         if  ( next )  next ( ) ; 
      }
   }) ; 
},
  getName:function ( ) {var _THIS_=this;
    if  ( (_THIS_.page))  {
       return  (_THIS_.page).name; 
    }
    return  null ; 
},
  setName:function ( n ) {var _THIS_=this;
    if  ( n == (_THIS_.getName.bind(_THIS_))( ) )  return  false ; 
    if  ( (_THIS_.page))  {
        (_THIS_.page).name= n ; 
        (_THIS_.setNameBox.bind(_THIS_))( ) ; 
        return  true ; 
    } else  debug ( "Page is null" ) ; 
    return  false ; 
},__dummy:false});