
var fibers=[]; 
var fiberStarted=false;
function fiber(state) {
   fiber.start();
   var _isDead=false;
   var f={state:setState, die:die, isDead:isDead,move:move,
          setThis:setThis, waitAjax:waitAjax};
   var _this=f;
   fibers.push(f);
   return f;
   function setState(s){state=s;}
   function iterate(iter,loop,next) {
      state=iter;
      
      loop;
      _iter=iter;
   }
   function move() {
     if(state instanceof Function) state(_this);
     else {debug("Not a func:");debug(state);}
   }
   function isDead() {return _isDead;}
   function die() {_isDead=true;}
   function setThis(t) {_this=t;}
   function waitAjax(resumeState) {
      state=NOP;
      return next;
      function next() {
         state=resumeState;
      }
   }
}
fiber.times=function(cnt,state) {
  fiber(s);
  function s(f) {
     cnt--;
     if (cnt<0) f.die();
     state(f);
  }
}
fiber._interval=20;
fiber.interval=function (i) {
    if (arguments.length==0) {
       return fiber._interval;
    } else {
       fiber._interval=i;
       fiber.stop();
       fiber.start();
    }
};
fiber.stop=function () {
  fiberStarted=false;
  clearInterval(fiber.timer);
};
function startFiber() {
   if (fiberStarted) return;
   fiberStarted=true;
   fiber.timer=setInterval(moveAll,fiber.interval());
   function moveAll() {
      var oldf=fibers;
      fibers=[];
      oldf.select(p).each(add);
      function add(e) {fibers.push(e);}
      function p(f) {
         if (f.isDead()) return false;
         f.move();
         return true;
      }
   }
}
fiber.start=startFiber;  