var DocumentScriptable;
var TextEditor2=Class.create({ 
  initialize:function ( _page  ,options ) {
	   if  ( arguments .length==1  || !( _page  instanceof  DocumentScriptable) )  {
	    	_page= options.page;
	   }
	   if (options) { Object .extend( this , options ) ; } 
	   _page.load( this.onPageReady.bind(this) ) ;    
  },
  onPageReady:function (_page ) {var _THIS_=this;
   if (_page) {this.page=_page;}
   if  ( (_THIS_.uis).body)  {
     var  tx = (_THIS_.uis).body.get( 0 ) ; 
     var  ind = "indentAdaptered" ; 
     if  ( ! jQuery .data( tx , ind ) )  {
        jQuery .data( tx , ind , true ) ; 
        attachIndentAdapter ( tx ) ; 
     }
     if (!this.page.body) {this.page.body="";}
     (_THIS_.uis).body.val(this.page.body) ; 
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
  getText:function () { 
  	  if (this.page) { return this.page.body; } 
      return "";
  },
  setNameBox:function ( ) {var _THIS_=this;
   var  n = (_THIS_.getName.bind(_THIS_))( ) ; 
   if  ( n  &&  (_THIS_.uis).name)  {
      (_THIS_.uis).name.val( (_THIS_.stripExt.bind(_THIS_))( n ) ) ; 
   }
},
  stripExt:function ( n ) {var _THIS_=this;
    if (!this.extension) return;
   if  ( n .endsWith( (_THIS_.extension)) )  {
      return  n .substring( 0 , n .length- (_THIS_.extension).length) ; 
   }
   return  n ; 
},
  appendExt:function ( n ) {var _THIS_=this;
    if (!this.extension) return;
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