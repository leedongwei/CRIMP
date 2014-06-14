var bodyParser = require('body-parser');
var express = require('express');
var pg = require('pg');
var config = require('./config.js');

var app = express();
app.use(bodyParser());


app.get('/judges/get/:round', function (req, res) {
	var message = {'climbers':[
									{"c_id":"NW001", "c_name":"Anna"},
									{"c_id":"NW002", "c_name":"Brenda"},
									{"c_id":"NW003", "c_name":"Cathy"},
									{"c_id":"NW004", "c_name":"Dorothy"},
									{"c_id":"NW005", "c_name":"Erica"},
									{"c_id":"NW006", "c_name":"Felicia"}
								]};

	var round = req.params.round;

	// Parameters checking
	if (round.length != 3) {
		res.send(400);
	} else {
		//TODO: call to database

		res.set('Content-Type', 'application/json');
		res.send(200, JSON.stringify(message));
	}
});


app.get('/judges/get/:c_id/:r_id', function (req, res) {
	var message = {	'c_name': '',
									'c_score': ''	};

	var c_id = req.params.c_id;
	var r_id = req.params.r_id;

	// Parameters checking
	if (c_id.length != 5 ||
			r_id.length != 5 ||
			c_id.substring(0,2) != r_id.substring(0,2)) {
		res.send(400);
	} else {
		var round = req.params.r_id.substring(0, 3); 	//First 3 char are round name
		var route = req.params.r_id.substring(3, 5);	//Last 2 char are route number

		//TODO: call to database
		//message.c_name = c_id;
		//message.c_score = route + round;
		message.c_name = 'Anna';
		message.c_score = '11111B11T';

		res.set('Content-Type', 'application/json');
		res.send(200, JSON.stringify(message));
	}


});


/*
	curl -H "Content-Type: application/json" -d "{\"j_name\":\"DW\", \"auth_code\":\"\",\"r_id\":\"NMQ01\",\"c_id\":\"NM128\",\"c_score\":\"111BT\"}" http://127.0.0.1:3000/judges/set
*/
app.post('/judges/set', function (req, res) {
	var postBody = req.body;

	// TODO: change to server logs
	console.log(JSON.stringify(postBody));

	// Parameters checking
	if (postBody.r_id.length != 5 ||
			postBody.c_id.length != 5 ||
			postBody.c_score.length > 64 ) {
		res.send(400);
	} else {
		// TODO: call to server

		res.send(200);
	}
});


app.get('/client/get/:round', function (req, res) {
	var message = {'climbers':[]};

	var round = req.params.round;

	// Parameters checking
	if (c_id.length != 3) {
		res.send(400);
	} else {
		//TODO: call to database
		message.climbers = 'yo';

		res.set('Content-Type', 'application/json');
		res.send(200, JSON.stringify(message));
	}

});



var crimp_port = Number(process.env.PORT || config.development.judge_port);
var server = app.listen(crimp_port, function() {
  console.log('Listening on port %d', server.address().port);
});