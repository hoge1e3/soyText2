var fiber,TagBuilderJ;
var Frame=Class.create({
   initialize: function (bar, content, opt) {
      var t=new TagBuilderJ();
      var closeButton=[
          "button",{onclick:this.close.bind(this)},"X"];
      var minimizeButton=[
          "button",{onclick:this.toggleMinimize.bind(this)},"_"];
      t.referenceObject(this);
      t.add(
        ["span#top",{style:{position:"absolute"}},
          ["div#tbarTop",
             {style: {
                color:"#ffffff",
                backgroundColor:"#4466cc",
                opacity:"0.75"
             }},
             ["span" ,closeButton,minimizeButton,["span#tbar",bar]]
          ],
          ["div#content",
            {style:{backgroundColor: "#dddddd"}},content]
        ]
      );
      var br=this.bringToTop.bind(this);
      this.top.draggable({handle: this.tbarTop ,start: br});
      this.tbarTop.click(br);
      this.top.appendTo("body");
   },
   toggleMinimize: function (b) {
      this.content.toggle(500);
   },
   close: function () {
      if (this.onClose) { this.onClose(); }
      this.top.fadeOut();
      this._closed=true;
   },
   closed: function(){return this._closed;},
   bringToTop:function () {
      this.top.appendTo("body");
   },
   jump: function () {
      var vy=-20,pop=true,oy=this.pos().y;
      function sw(f) {
        var p=this.pos();
        if (p.y>oy) {
           this.pos(p.x,oy);
           f.die();
           return;
        }
        this.pos(p.x, p.y+vy);
        vy+=4;
        if (pop && vy>=0) {
           this.bringToTop();
           pop=false;
        }
      }
      fiber(sw.bind(this).pErrf());
   },
   setTitleBar: function (c) {
      var t=new TagBuilderJ();
      t.referenceObject(this);
      t.add(c);
      //debug(c);
      this.tbar.empty().append(t.jqObject);
   },
   setContent: function (c) {
      var t=new TagBuilderJ();
      t.referenceObject(this);
      t.add(c);
      this.content.empty().append(t.jqObject);
   },
   pos: function (x,y) {
      if (arguments.length==0) {
         return {
            x: parseInt(this.top.css("left").replace(/px/)),
            y: parseInt(this.top.css("top").replace(/px/))
         };         
      }
      this.top.css({left:x, top:y});
   }

});