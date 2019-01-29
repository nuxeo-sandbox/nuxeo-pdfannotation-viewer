var hostUrl = window.location.protocol + '//' + window.location.host;

var docUrl = hostUrl + '/pdf.js/web/';

var restUrl = hostUrl + '/nuxeo/api/v1/';

var username = 'Unknown';

var annotationSaveUrl = 'annotation';

var annotationDeleteUrl = 'annotation';

var commentSaveUrl = 'annotationComment';

var printurl = null;

function initURLS(){
  var urlParams = new URLSearchParams(window.location.search);
  var file = urlParams.get("file");
  printurl = file;
  if (file) {
	var pieces = file.split("/");
	var fragment = 'annotation/' + pieces[10];
	if (pieces[11] === '@preview') {
      fragment += "?xpath=file:content";
	} else if (pieces[11] === '@blob') {
      fragment += "?xpath=";
      var idx = 12;
      var sep = "";
      while (idx < pieces.length && pieces[idx] !== '@preview') {
    	  fragment += sep + pieces[idx];
    	  idx += 1;
    	  sep = "/";
      }
	}
    annotationSaveUrl = fragment;
    annotationDeleteUrl = fragment;
  }
  $.get(restUrl + 'me', function(data) { username = data.id; }, 'json');
}
