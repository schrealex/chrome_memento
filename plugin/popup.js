(function()
{

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


  var range = null;
  var segments = [];
  var buf = [];
  var ix = 0;

  var eventualImage = function(data)
  {
    var deferred = Q.defer();

    var image = new Image();

    image.onload = function()
    {
      deferred.resolve(image);
    }

    image.src = data;

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

  chrome.runtime.sendMessage(null, 'lock', function(message)
  {
    if(typeof message != 'object)
    {
      return;
    }

    var type = message.type;

    if(type == 'range')
    {
      range = message.range;
    }

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

})();
