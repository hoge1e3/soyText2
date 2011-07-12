var SoyText,toLines,sprintf,toHashLiteral;
var DocumentScriptable=Class.create({ 
  initialize:function ( _id ) {
  	  this.id=_id;
 	  this._loaded= false ; 
  },
  load:function ( next ) {
	  var  t = this ; 
	  if  ( next ) { return  t.load().next( next ); } 
	  var  url = sprintf ( "%s/byId/%s?json" , SoyText .rootPath( ) , t.id) ; 
	  return  $ .get( url , {}) .next( function  ( r )  {
	     toLines ( r ) .each( function  ( l )  {
	        if  ( l .match( /lastupdate: *(\d+)/ ) )  {
	           t .lastUpdate= parseInt ( RegExp .$1) ; 
	        }
	        if  ( l .match( /^\$/ ) )  {
	           var  $ = SoyText ; 
	           var  _ = t ; 
	           eval ( l ) ; 
	        }
	     }) ; 
	     t._loaded= true ; 
	     return  t ;      
	  }) ; 
  },
  get:function ( key ) {
	  if  ( !this._loaded)  throw  sprintf ( "%s Not loaded" , this ) ; 
	  return  this [ key ] ; 
  },
  put:function ( key ,value ) {var _THIS_=this;
  this [ key ] = value ; 
},
  save:function ( next ) {var _THIS_=this;
	  if  ( next )  { return  this.save().next( next ) ;} 
	  this.__test=function () { return next; };
	  var  url = sprintf ( "%s/byId/%s" , SoyText .rootPath( ) , this.id) ; 
	  var  s = SoyText.generateContent(this); 
	  return  $.post( url , {content : s }) ; 
	},
  compile:function ( ) {var _THIS_=this;

}});