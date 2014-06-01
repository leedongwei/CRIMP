var bodyParser = require('body-parser');
var express = require('express');
/*var pg = require('pg');*/
var config = require('./config.js');

var app = express();
app.use(bodyParser());


app.get('/judges/get/:c_id/:r_id', function (req, res) {
	var message = {	'http_code': '200',
									'c_name': '',
									'c_score': ''	};

	var c_id = req.params.c_id;
	var r_id = req.params.r_id;

	// Parameters checking
	if (c_id.length != 5 ||
			r_id.length != 5 ||
			c_id.substring(0,2) != r_id.substring(0,2)) {
		message.http_code = '400';
	} else {
		var round = req.params.r_id.substring(0, 3); 	//First 3 char are round name
		var route = req.params.r_id.substring(3, 5);	//Last 2 char are route number

		//TODO: call to database
		message.http_code = '200';
		message.c_name = c_id;
		message.c_score = route + round;
	}

	res.send(JSON.stringify(message));
});


/*
	curl -H "Content-Type: application/json" -d "{\"j_name\":\"DW\", \"auth_code\":\"\",\"r_id\":\"NMQ01\",\"c_id\":\"NM128\",\"c_score\":\"111BT\"}" http://127.0.0.1:3000/judges/set
*/
app.post('/judges/set', function (req, res) {
	var message = {'http_code':'200'};
	var postBody = req.body;

	// TODO: change to server logs
	console.log(JSON.stringify(postBody));

	// Parameters checking
	if (postBody.r_id.length != 5 ||
			postBody.c_id.length != 5 ||
			postBody.c_score.length > 64 ) {
		message.http_code = '400';
	} else {
		// TODO: call to server

	}

	res.send(message);
});


app.get('/client/get/:round', function (req, res) {
	var message = {	'http_code': '200',
									'climbers':''	};

	var round = req.params.round;

	// Parameters checking
	if (c_id.length != 3) {
		message.http_code = '404';
	} else {
		//TODO: call to database
		message.http_code = '200';
		message.climbers = 'yo';
	}

	res.send(JSON.stringify(message));
});


var server = app.listen(config.development.judge_port, function() {
  console.log('Listening on port %d', server.address().port);
});