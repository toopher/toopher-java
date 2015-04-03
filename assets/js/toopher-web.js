(function(window, $){

  var postToUrl = function (path, params, method){
    method = method || 'POST';
    var form = $('<form />').attr('method', method).attr('action', path);
    for (var key in params){
      if (params.hasOwnProperty(key)){
        var hiddenField = $('<input />').attr('type', 'hidden').attr('name', key).attr('value', params[key]);
        form.append(hiddenField);
      }
    }
    $('body').append(form);
    form.submit();
  }

  var handleMessage = function(e){
    var msgData = JSON.parse(e.data);
    if (msgData.status === 'toopher-api-complete'){
      var iframe = $('#toopher_iframe');
      var frameworkPostArgsJSON = iframe.attr('framework_post_args');
      var frameworkPostArgs = {};
      if(frameworkPostArgsJSON){
        frameworkPostArgs = $.parseJSON(frameworkPostArgsJSON);
      }
      var postData = $.extend({}, msgData.payload, frameworkPostArgs);
      var toopherData = {'toopher_iframe_data': $.param(postData)};

      if(iframe.attr('use_ajax_postback')){
      $.post(iframe.attr('toopher_postback'), toopherData)
        .done(function(data){
          data = $.parseJSON(data);
        });
      } else {
        postToUrl(iframe.attr('toopher_postback'), toopherData, 'POST');
      }
    }
  }

  if (window.addEventListener) {
    window.addEventListener('message', handleMessage, false);
  } else {
    window.attachEvent('onmessage', handleMessage);
  }

})(window, jQuery);
