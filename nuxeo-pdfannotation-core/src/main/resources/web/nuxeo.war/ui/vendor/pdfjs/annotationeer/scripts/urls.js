var hostUrl = window.location.protocol + '//' + window.location.host;

var docUrl = hostUrl + '/pdf.js/web/';

var restUrl = hostUrl + '/nuxeo/api/v1/';

var username = 'Unknown';

var annotationSaveUrl = 'annotation';

var annotationDeleteUrl = 'annotation';

var commentSaveUrl = 'annotationComment';

var printurl = null;

function initURLS() {
	console.log("Search: " + window.location.search);
	var urlParams = new URLSearchParams(window.location.search);
	var file = urlParams.get("file");
	console.log("File: " + file);
	printurl = file;
	if (file) {
		var pieces = file.split("/");
		var fragment = "";
		if (pieces[4] === 'nxfile') {
			fragment = 'annotation/' + pieces[6];
			fragment += "?xpath=file:content";
		} else {
			fragment = 'annotation/' + pieces[10];
		}
		if (pieces.length >= 11 && pieces[11] === '@blob') {
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
	console.log("REST: " + restUrl);
	$.get(restUrl + 'me', function(data) {
		username = data.id;
	}, 'json');
}
