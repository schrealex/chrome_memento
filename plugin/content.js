(function()
{
  /*
   * Communicated state
   */
  var state = {
    mouseX : 0,
    mouseY : 0,
    screenX : 0,
    screenY : 0,
    scrollX : 0,
    scrollY : 0
  }

  document.addEventListener('mousemove', function(e)
  {
    state.mouseX = e.x;
    state.mouseY = e.y;

    state.screenX = e.screenX;
    state.screenY = e.screenY;
  });

  chrome.extension.onMessage.addListener(function(message, sender, sendResponse)
  {
    try
    {
      if (message.action == "state")
      {
        state.scrollX = window.scrollX;
        state.scrollY = window.scrollY;
        sendResponse(state);
      }
      else
      {
        sendResponse({});
      }
    }
    catch(ex)
    {
    }

    return true;
  });
})();
