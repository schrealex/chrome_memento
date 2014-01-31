(function() 
{
  "use strict";

  chrome.storage.local.clear();
  
  var isObject = function(object)
  {
    return typeof object === "object";
  };

  var extensionId = chrome.i18n.getMessage("@@extension_id");

  // 220MiB top (write first, remove later, 3 bufs of 25 frames (~75 frames =~= 8s)
  var bufSize = 600;
  var shortBufSize = 25;

  var segments = [];
  var segmentDimensions = [];

  var segmentId = 0;
  
  var isLocked = false;

  var sessionId = 0;
  var recordingId = 0;
  var activeTabId = null;
  var buf = [];
  var segmentDimension = { width : 0, height : 0};

  /*
   * Window promise (in : id, out : window)
   */
  var windowById = function(id)
  {
    var deferred = Q.defer();

    chrome.windows.get(id, null, function(window)
    {
      deferred.resolve(window);
    });

    return deferred.promise;
  };

  /*
   * Tab promise (in : id, out : tab)
   */
  var tabById = function(id)
  {
    var deferred = Q.defer();

    if(id == null)
    {
      deferred.resolve(null);
    }
    else
    {
      chrome.tabs.get(id, function(tab)
      {
        deferred.resolve(tab);
      });
    }

    return deferred.promise;
  };

  var isWebTab = function(tabId)
  {
    return tabById(tabId)
    .then(function(tab)
    {
      return Q.fcall(function()
      {
        return tab != null && tab.url.substr(0, 4) === 'http';
      });
    });
  }

  /*
   * Screenshot promise
   */
  var capture = function()
  {
    return isWebTab(activeTabId)
    .then(function(isWebTab)
    {
      var deferred = Q.defer();

      if(isWebTab)
      {
        chrome.tabs.captureVisibleTab(null, null, function(data)
        {
          deferred.resolve(data);
        });
      }
      else
      {
        deferred.resolve(undefined);
      }

      return deferred.promise;
    });
  };

  var canConnectPromise = function(tab)
  {
    var deferred = Q.defer();

    try
    {
      var port = chrome.tabs.connect(tab.id);
      port.disconnect();
      deferred.resolve(true);
    } catch(e)
    {
      deferred.resolve(false);
    }

    return deferred.promise;
  }

  /*
   * promise (state) : state if available
   */
  var state = function(tab)
  {
      var deferred = Q.defer();

      var timeout = setTimeout(function()
      {
        deferred.resolve(undefined);
      }, 250);

      chrome.tabs.sendMessage(tab.id, {action : 'state'}, function(response)
      {
        if(typeof response === 'undefined')
        {
          console.log('some kind of failure');
        }
        clearTimeout(timeout);
        deferred.resolve(response);
      });
      return deferred.promise;
  };

  /*
   * Start recording for a specific tab
   */
  var onTabChanged = function(windowId, tabId)
  {
    sessionId++;

    activeTabId = tabId;

    var thisSessionId = sessionId;

    var ref = {};

    ref.callback = function()
    {
      var start = new Date();

      Q.allSettled([windowById(windowId), tabById(tabId)])
      .spread(function(window, tab)
      {
        window = window.value;
        tab = tab.value;

        canConnectPromise(tab).then(function(canConnect)
        {

          if(!canConnect)
          {
            return;
          }

          return state(tab)
          .then(function(state)
          {
            return capture()
            .then(function(data)
            {
              if(data && state && !isLocked)
              {
                console.log('storing frame!');
                if(segments.length > bufSize / shortBufSize)
                {
                  var id = segments.shift();
                  segmentDimensions.shift();
                  chrome.storage.local.remove(id);
                }
  
                var obj = {
                  title   : tab.title,
                  url     : tab.url,
                  top     : window.top,
                  left    : window.left,
                  width   : window.width,
                  height  : window.height,
                  mouseX  : state.mouseX,
                  mouseY  : state.mouseY,
                  screenX : state.screenX,
                  screenY : state.screenY,
                  scrollX : state.scrollX,
                  scrollY : state.scrollY,
                  data    : data,
                  time    : start.getTime()
                };
  
                buf.push(obj);

                segmentDimension.width = Math.max(obj.width, segmentDimension.width);
                segmentDimension.height = Math.max(obj.height, segmentDimension.height);
  
                if(buf.length == shortBufSize)
                {
                  var data = {};
                  var id = '' + segmentId;
                  data[id] = buf;
  
                  segments.push(id);
                  segmentDimensions.push(segmentDimension);

                  chrome.storage.local.set(data, function()
                  {
                    console.log('stored data in local storage');
                  });
  
                  segmentId++;
                  buf = [];
                  segmentDimension = {width : 0, height : 0};
                }
              }
  
              // Only continue if needed
              if(sessionId == thisSessionId)
              {
                var end = new Date();
  
                var diff = end.getTime() - start.getTime();
                setTimeout(ref.callback, Math.max(1, 100 - diff));
              }
            });
          });
          
        });
      }).done();
    };
    ref.callback();
  }


  /*
   * Start recording on tab change
   */
  chrome.tabs.onActivated.addListener(function(activeInfo)
  {
    onTabChanged(activeInfo.windowId, activeInfo.tabId);
  });

  chrome.runtime.onMessage.addListener(function(message, sender, sendResponse)
  {
    if(!isObject(message))
    {
      return;
    }

    if(sender.id !== extensionId)
    {
      return;
    }

    var type = message.type;

    if (type === 'size') {

      var width = segmentDimension.width;
      var height = segmentDimension.height;

      for(var i = 0; i < segmentDimensions.length; i++)
      {
        var width = Math.max(width, segmentDimensions[i].width);
        var height = Math.max(height, segmentDimensions[i].height);
      }
      
      sendResponse({width: width, height:height});
    }
    
    if(type === 'segments')
    {
      var popupSegments = [];

      segments.forEach(function(segment)
      {
        popupSegments.push(segment);
      });
      
      popupSegments.push(segmentId);

      //lockedSegments.push(popupSegments);

      sendResponse({ 'segments' : popupSegments } );

      return true;
    }

    if(type === 'fetch')
    {
      console.log('executing fetch');
      var id = message.segmentId;
      
      if (id === segmentId) {
        console.log('should send latest segment');
        sendResponse(buf);
        return;
      }
      
      console.log('sending id', id);
      chrome.storage.local.get('' + id, function(segment)
      {
        
        console.log('should send segment', segment);
        sendResponse(segment[''+id]);
      });
      return true;
      // fetch and return the frame
      
    }
  });

  /*
   * Invoked when popup opens and closes
   */
  chrome.runtime.onConnect.addListener(function(port)
  {
    isLocked = true;
    
    port.onDisconnect.addListener(function() {
      isLocked = false;
    });
  });
  
  /*
   * Kicker for window modifications
   */
  var onFocusChangedListener = function(windowId)
  {
    activeTabId = null;

    if(windowId === -1)
    {
      return;
    }

    chrome.tabs.query({active: true, windowId : windowId}, function(tabs)
    {
      if(tabs !== null && tabs.length > 0)
      {
        onTabChanged(windowId, tabs[0].id);
      }
    });
  }

  chrome.windows.onFocusChanged.addListener(onFocusChangedListener);

  // Start recording right away
  chrome.windows.getCurrent(function(obj) { onFocusChangedListener(obj.id) });

})();
