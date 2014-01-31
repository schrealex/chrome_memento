(function()
{
  var isObject = function(object)
  {
    return typeof object === "object";
  };

  var postVideo = function(blob)
  {
    console.log('posting video...');
    var deferred = Q.defer();
    console.log('posting video');
    var http = new XMLHttpRequest();
				
    var postUrl = "http://172.25.66.185:9222/api/game";

				
    var form = new FormData();
    var request = new XMLHttpRequest();

    form.append("file", blob, "video.webM");
				
    request.open("POST", postUrl, true);				
    request.setRequestHeader('Access-Control-Allow-Origin', 'value');
    request.setRequestHeader('X-Custom-Header', 'value');
				
    request.send(form);
				
    http.onreadystatechange = function() {
      console.log('finished uploading');
      //  && http.status == 200
      if(http.readyState == 4) {
        deferred.resolve(true);
        alert(http.responseText);
      }
    }
    
    return deferred.promise;
				/* http.open("POST", postUrl, true);

				//Send the proper header information along with the request
				http.setRequestHeader("Content-type", "application/x-www-form-urlencoded"); */


  };
  
  var eventualSegment = function(id)
  {
    var deferred = Q.defer();

    chrome.storage.local.get(id, function(segment)
    {
      deferred.resolve(segment);
    });

    return deferred.promise;
  }

  var eventualCanvas = function()
  {
    var deferred = Q.defer();

    var canvas = document.getElementById('canvas');

    if(canvas == null)
    {
      window.onload = function()
      {
        deferred.resolve(document.getElementById('canvas'));
      };
    }
    else
    {
      deferred.resolve(canvas);
    }

    return deferred.promise;
  }
  
  /*
   * Fetch dimension
   */
  var eventualDimension = function()
  {
    var deferred = Q.defer();
    
    chrome.runtime.sendMessage(null, {type : 'size'}, function(response)
    {
       deferred.resolve(response);
    });
    
    return deferred.promise;
  }
  
  var eventualSegments = function()
  {
    var deferred = Q.defer();
    
    chrome.runtime.sendMessage(null, {type : 'segments'}, function(message)
    {
      if(!isObject(message))
      {
        deferred.reject(new Error("No segments received"));
        return;
      }
      else
      {
        deferred.resolve(message.segments);
      }
    });
    
    return deferred.promise;
  };
  
  var fetch = function(id)
  {
    var deferred = Q.defer();

    chrome.runtime.sendMessage(null, {type : 'fetch', segmentId : id}, function(response)
    {
      deferred.resolve(response);
    });
    
    return deferred.promise;
  };

  /*
   * Fetch segment
   */
  
  var range = null;
  var segments = [];
  var buf = [];
  var ix = 0;

  var eventualImage = function(frame)
  {
    var deferred = Q.defer();

    var image = new Image();

    image.onload = function()
    {
      frame.raw = image;

      deferred.resolve(frame);
    }

    image.src = frame.data;

    return deferred.promise;
  }

  var repaint = function()
  {
    console.log('repaint!');
    eventualCanvas()
    .then(function(canvas)
    {
      var context = canvas.getContext('2d');

      if(buf.length <= ix)
      {
        context.fillStyle = 'white';
        context.fill();

        return;
      }

      var frame = buf[ix].data;

      return eventualImage(frame)
      .then(function(image)
      {
        var width = image.width;
        var height = image.height;

        var wFact = 360 / width;
        var hFact = 360 / height;
        var fact = Math.min(wFact, hFact);

        width *= fact;
        height *= fact;

        context.fillStyle = 'white';
        context.fillRect(0, 0, 360, 360);

        context.drawImage(image, 0, 0, image.width, image.height, (360 - width) / 2, (360 - height) / 2, width, height);

        var progress = (ix+1)/buf.length;

        context.fillStyle = "rgba(0, 0, 255, 0.5)";
        context.fillRect(0, 0, progress * 360, 20);

        var text = buf[ix].title;
        var fontBase = 'pt Arial';

        var pt = 12;

        for(; pt > 6; pt--)
        {
          context.font = pt + fontBase;
          var metrics = context.measureText(text);

          if(metrics.width < 350)
          {
            break;
          }
        }

        context.fillStyle = "rgba(255, 255, 255, 0.5)";
        context.fillRect(0, 360 - (pt + 3), 360, 360);

        context.fillStyle = 'black';
        context.fillText(text, 3, 360 - pt);

      });

    }).done();
  };

  document.addEventListener("mousewheel", function(event)
  {
    var delta = 0;

    if(event.wheelDeltaY > 0)
    {
      delta = 1;
    }
    else if(event.wheelDeltaY < 0)
    {
      delta = -1;
    }

    if(delta == 0 && event.wheelDeltaX < 0)
    {
      delta = 10;
    }
    else if(delta == 0 && event.wheelDeltaX > 0)
    {
      delta = -10;
    }

    ix += buf.length;
    ix += delta;
    ix %= buf.length;

    repaint();
  });

  chrome.runtime.connect({});
  
  var rendered = 0;
  
  eventualDimension().then(function(dimension) {
    eventualSegments().then(function(segments)
    {
      var segmentCount = segments.length + 1;
      console.log(segments);

      eventualCanvas().then(function(canvas)
      {
        canvas.width = dimension.width;
        canvas.height = dimension.height;
        
        var video = new Whammy.Video(10);
        
        // fetch segments
        
        var renderFrames = function(frames)
        {

          var addFrame = function(data)
          {
            var image = data.raw;

            // center image, draw mouse, etc?
            canvas.getContext('2d').drawImage(image, 0, 0);
            video.add(canvas);
            
            //
            rendered++;
            
            var status = document.getElementById('status');
            //console.log(status);
            status.innerHTML = 'rendering ' + ~~(100 * (rendered/ (segmentCount * 25))) + '%';
            
  
            if (frames.length === 0) {
              if (segments.length > 0) {
                var segmentId = segments.shift();
            
                fetch(segmentId).then(renderFrames).done();
              }
              else
              {
                status.innerHTML = 'compiling';
                // done!
                var output = video.compile();
              
                var url = webkitURL.createObjectURL(output);
                document.getElementById('awesome').src = url;
                status.innerHTML = 'done';
                postVideo(output).done();
              }
              return;
            }
            
            var frame = frames.shift();

            eventualImage(frame).then(addFrame).done();
          }
          
          if (frames.length === 0) {
            return;
          }
  
          var frame = frames.shift();
          
          eventualImage(frame).then(addFrame).done();
        };
        
        var segmentId = segments.shift();

        fetch(segmentId).then(renderFrames).done();

      }).done();
    
    return;
            
      chrome.runtime.sendMessage(null, {type : 'fetch', segmentId : segments[0]}, function(response)
      {
        eventualCanvas().then(function(canvas)
        {
          console.log('hello!', response);
          var video = new Whammy.Video(10);
          
         
          
          return;
          
          console.log(response);
          for(var i = 0; i < response.length; i++)
          {
            console.log(response[i]);
          }
          eventualImage(response[0].data).then(function(image)
          {
            canvas.getContext('2d').drawImage(image, 0, 0);
            video.add(canvas);
            var output = video.compile();
            postVideo(output).done();
            console.log(output);
            var url = webkitURL.createObjectURL(output);
  
            try {
              console.log(canvas.toDataURL("image/webp"));
            } catch(e) {
              console.log(e);
            }
  
          });
        }).done();
        console.log('fetched response', response);
      });
  
      return;
    
    
    
      eventualSegment(popupSegments[0]).then(function(segment)
      {
        console.log(segment);
        buf = segment;
        ix = 0;
        repaint();
      });
  /*    chrome.storage.local.get(key, function(data)
      {
        console.log('got',data);
  //      buf = data[key];
  //      ix = 0;
  //      repaint();
      });*/                     
    });
  });
  
  return;
  // init
  chrome.runtime.sendMessage(null, {type : 'segments'}, function(message)
  {
    if(!isObject(message))
    {
      return;
    }


  });

})();
