var currentPage = 0;
var imgURLs;

var mangoCompanionUrl = "ip:port";

function listChapters(query) {
	var json = {};
	var URL = 'http://' + mangoCompanionUrl + '/api/mango/listChapters?url=' + query;
	try {
		json = JSON.parse(mango.get(URL).body);
		if(json.error != null){
			mango.raise('Error: ' + json.error);
		}
		return JSON.stringify(json);
	} catch (e) {
		mango.raise('Failed to get JSON from ' + URL);
	}
}

function selectChapter(id) {
	var json = {};
	var URL = 'http://' + mangoCompanionUrl + '/api/mango/selectChapter?id=' + id;
	try {
		json = JSON.parse(mango.get(URL).body);
	} catch (e) {
		mango.raise('Failed to get JSON from ' + URL);
	}

	if(json.error != null){
		mango.raise('Error: ' + json.error);
	}

	imgURLs = json.pages;
	currentPage = 0;

	var info = {
		title: json.title,
		pages: imgURLs.length
	};
	return JSON.stringify(info);
}

function nextPage() {
	if (currentPage >= imgURLs.length)
		return JSON.stringify({});

		var url = imgURLs[currentPage]
		var fn = currentPage + '.png';
	var info = {
		filename: fn,
		url: url
	};

	currentPage += 1;
	return JSON.stringify(info);
}
