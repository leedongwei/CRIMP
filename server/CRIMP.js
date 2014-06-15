var bodyParser = require('body-parser'),
		express = require('express'),
		pg = require('pg'),
		config = require('./config.js');

var app = express();
app.use(bodyParser());

var dbConn = process.env.DATABASE_URL || config.development.db_conn;
console.log(dbConn);


app.get('/judges/get/:round', function (req, res) {

	if (req.params.round.length != 3) {
		console.error('400: Parameter Error');
		res.send(400);
		return;
	}

	var	message = {'climbers':[]},
			queryConfig = {
				'text': 'SELECT c_id, c_name FROM crimp_data ' +
							'WHERE c_category = $1;',
				'values': [req.params.round.substring(0,2)]
			};

	pg.connect(dbConn, function(err, client, done) {
		if (err) {
			console.error('500: Error fetching client from pool');
			res.send(500);
			return;
		}

		client.query(queryConfig, function(err, result) {
			// IMPORTANT! Release client back to pool
			done();

			if (err) {
				console.error('500: Error running query', err);
				res.send(500);
			} else if (!result) {
				console.error('404: Data not found');
				res.send(404);
			} else {
				result.rows.forEach(function(entry) {
					message.climbers.push({
						'c_id': entry.c_id, 'c_name:': entry.c_name
					});
				});

				res.set('Content-Type', 'application/json');
				res.send(200, JSON.stringify(message));
			}
		});
	});
});


app.get('/judges/get/:c_id/:r_id', function (req, res) {
	var c_id = req.params.c_id,
			r_id = req.params.r_id;

	if (c_id.length != 5 ||
			r_id.length != 5 ||
			c_id.substring(0,2) != r_id.substring(0,2)) {
		console.error('400: Parameter Error');
		res.send(400);
		return;
	}

	var route = req.params.r_id.substring(2, 5) + '_raw',
			message = {	'c_name': '', 'c_score': ''	},
			queryConfig = {
				'text': 'SELECT ' + route +
								', c_name FROM crimp_data ' +
								'WHERE c_id = $1;',
				'values': [c_id]
			};

	pg.connect(dbConn, function(err, client, done) {
		if (err) {
			console.error('500: Error fetching client from pool');
			res.send(500);
			return;
		}

		client.query(queryConfig, function(err, result) {
			// IMPORTANT! Release client back to pool
			done();

			if (err) {
				console.error('500: Error running query', err);
				res.send(500);
			} else if (!result) {
				console.error('404: Data not found');
				res.send(404);
			} else {
				message = result.rows[0];

				res.set('Content-Type', 'application/json');
				res.send(200, JSON.stringify(message));
			}
		});
	});
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



var serverPort = Number (process.env.PORT || config.development.judge_port);
var server = app.listen(serverPort, function() {
  console.log('Listening on port %d', server.address().port);
});