TraceDisplay2=Class.create({ 
  initialize:function ( e ) {var _THIS_=this;
  if  ( e .displayed)  return ; 
  e .displayed= true ; 
  (_THIS_.frame)= new  Frame ( "エラー:" , [ 
    [ "div" , "" + e ] , 
    [ "div#list" ] , 
    [ "div#src" ] 
  ] ) ; 
  var  a = stackTrace ( e ) ; 
  var  auto = true ; 
  a .each( ad ) ; 
  function  ad ( s )  {
     
     if  ( s .match( /([^\/]+)__[^:]*:(\d+)/ ) )  {
       var  d = SoyText .byId( RegExp .$1) ; 
       (_THIS_.frame).list.append( c ( d , RegExp .$2, auto ) ) ; 
       auto = false ; 
     }
  }
  function  c ( d ,  lin , auto )  {
     var  res = $t ( [ "div" ] ) ; 
     d .load( function  ( )  {
        res .text( d .name+ ":" + lin ) ; 
        res .click( open ) ; 
        if  ( auto )  open ( ) ; 
        function  open ( )  {
           (_THIS_.showError.bind(_THIS_))( d .body, lin ) ; 
        }
     }) ;      
     return  res ; 
  }
},
  showError:function ( src ,line ) {var _THIS_=this;
  (_THIS_.frame).src.text( "" ) ; 
  var  lines = src .split( /\n/ ) ; 
  (_THIS_.frame).src.append( $t ( 
     [ "pre" , 
       
       
       function  ( t , tb )  {
          lines .each( function  ( e , idx )  {
             if  ( idx + 1 == line )  tb .add( [ "font" , {color : "red" }, "★" ] ) ; 
             tb .add( e + "\n" ) ; 
          }) ; 
       }
     ] 
  ) ) ; 
},__dummy:false});