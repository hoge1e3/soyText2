DocumentScriptable=Class.create({ 
 
 
 
  initialize:function ( _id ) {var _THIS_=this;
  (_THIS_.id)= _id ; 
  (_THIS_._loaded)= false ; 
},
  load:function ( next ) {var _THIS_=this;
  var  t = this ; 
  if  ( next )  return  (_THIS_.load.bind(_THIS_))( ) .next( next ) ; 
  var  url = sprintf ( "%s/byId/%s?json" , SoyText .rootPath( ) , (_THIS_.id)) ; 
  return  $ .get( url , {}) .next( function  ( r )  {
     toLines ( r ) .each( function  ( l )  {
        if  ( l .match( /lastupdate: *(\d+)/ ) )  {
           t .lastUpdate= parseInt ( RegExp .$1) ; 
        }
        if  ( l .match( /^\$/ ) )  {
           var  $ = SoyText ; 
           _ = t ; 
           eval ( l ) ; 
        }
     }) ; 
     (_THIS_._loaded)= true ; 
     return  t ;      
  }) ; 
},
  get:function ( key ) {var _THIS_=this;
  if  ( ! (_THIS_._loaded))  throw  sprintf ( "%s Not loaded" , this ) ; 
  return  this [ key ] ; 
},
  put:function ( key ,value ) {var _THIS_=this;
  this [ key ] = value ; 
},
  save:function ( next ) {var _THIS_=this;
  if  ( next )  return  (_THIS_.save.bind(_THIS_))( ) .next( next ) ; 
  var  url = sprintf ( "%s/byId/%s" , SoyText .rootPath( ) , (_THIS_.id)) ; 
  var  s = toHashLiteral ( this ) ; 
  s = "$.extend(_," + s + ");" ; 
  alert ( s ) ; 
  return  $ .post( url , {content : s }) ; 
},
  compile:function ( ) {var _THIS_=this;

},__dummy:false});