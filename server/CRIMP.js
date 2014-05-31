var express = require('express');
var bodyParser = require('body-parser');

var app = express();
app.use(bodyParser());


app.get('/judges/get/:c_id/:r_id', function (req, res) {
	var message={	'status': '200',
								'c_name': '',
								'c_score': ''};

	var c_id = req.params.c_id;
	var r_id = req.params.r_id;

	// Parameters checking
	if (c_id.length != 5 || r_id.length != 5) {
		message['status'] = '400';
	} else {
		var round = req.params.r_id.substring(0, 3); 	// First 3 char are round name
		var route = req.params.r_id.substring(3, 5);	// Last 2 char are route number

		//TODO: call to database
		message['status'] = '200';
		message['c_name'] = c_id;
		message['c_score'] = route + round;
	}

	res.send(JSON.stringify(message));
});


app.post('/judges/set', function (req, res) {
	/*
	curl -H "Content-Type: application/json" -d '{"stat":"200"}' http://127.0.0.1:3000/judges/set
	curl -H "Content-Type: application/json" -d '{"j_name":"judge's name","auth_code":"","r_id":"route_id","c_id":"climber_id","c_score":"magic_string"}' http://localhost:3000/judges/set
	*/

	/*var msgBody = JSON.parse(req.body);
	console.log('received POST from ' + msgBody[j_name]);*/
	console.log(req.body);
	res.send(req.body);
});


var server = app.listen(3000, function() {
    console.log('Listening on port %d', server.address().port);
});