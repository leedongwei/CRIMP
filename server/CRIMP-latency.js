var bodyParser = require('body-parser'),
		express = require('express'),
		pg = require('pg'),
		config = require('./config.js');

var app = express();
app.use(bodyParser());

var dbConn = process.env.DATABASE_URL || config.development.db_conn;

app.get('/judge/get/:round', function (req, res) {
	res.set('Content-Type', 'application/json');

	if (req.params.round.length !== 3) {
		//console.error('400: Parameter Error');
		res.send(400);
		return;
	}

	var	message = {'climbers':[]},
			queryConfig = {
				'text': 'SELECT c_id, c_name FROM crimp_data ' +
								'WHERE c_category = $1;',
				'values': [req.params.round]
			};

	pg.connect(dbConn, function(err, client, done) {
		if (err) {
			console.error('500: Error fetching client from pool', err);
			res.send(500);
			return;
		}

		client.query(queryConfig, function(err, result) {
			// IMPORTANT! Release client back to pool
			done();

			if (err) {
				//console.error('400: Error running query', err);
				res.send(400);;
			} else if (result.rowCount === 0) {
				//console.error('404: Data not found');
				res.send(404);
			} else {
				result.rows.forEach(function(entry) {
					message.climbers.push({
						'c_id': entry.c_id, 'c_name': entry.c_name
					});
				});

				//res.send(200, JSON.stringify(message));

				// Simulating latency
				setTimeout(function(){res.send(200, JSON.stringify(message))}, 5000);
			}
		});
	});
});


app.get('/judge/get/:c_id/:r_id', function (req, res) {
	var c_id = req.params.c_id,
			r_id = req.params.r_id;

	res.set('Content-Type', 'application/json');

	if (c_id.length !== 5 ||
			r_id.length !== 5 ||
			c_id.substring(0,2) !== r_id.substring(0,2) ||
			/^[a-z]+$/i.test(r_id.substring(2,3)) {
		//console.error('400: Parameter Error');
		res.send(400);
		return;
	}


	var route = req.params.r_id.substring(2, 5).toLowerCase() + '_raw',
			message = {	'c_name': '', 'c_score': ''	},
			queryConfig = {
				'text': 'SELECT c_name, ' + route +
								' FROM crimp_data ' +
								'WHERE c_id = $1;',
				'values': [c_id]
			};

	pg.connect(dbConn, function(err, client, done) {
		if (err) {
			console.error('500: Error fetching client from pool', err);
			res.send(500);
			return;
		}

		client.query(queryConfig, function(err, result) {
			// IMPORTANT! Release client back to pool
			done();

			if (err) {
				//console.error('400: Error running query', err);
				res.send(400);
			} else if (result.rowCount === 0) {
				//console.error('404: Data not found');
				res.send(404);
			} else {

				message.c_name = result.rows[0]['c_name'];

				if (result.rows[0][route]) {
					message.c_score = result.rows[0][route];
				}

				//res.send(200, JSON.stringify(message));

				// Simulating latency
				setTimeout(function(){res.send(200, JSON.stringify(message))}, 5000);
			}
		});
	});
});


/*
	curl -H "Content-Type: application/json" -d "{\"j_name\":\"DW\", \"auth_code\":\"crimpAUTH\",\"r_id\":\"NMQ01\",\"c_id\":\"NM001\",\"c_score\":\"111BT\"}" http://127.0.0.1:3000/judge/set
*/
app.post('/judge/set', function (req, res) {
	var postBody = req.body;

	res.set('Content-Type', 'application/json');

	// TODO: server log judge's name

	if (postBody.auth_code !== config.development.auth_code) {
		console.error('401 Unauthorized');
		res.send(401)
		return;
	}

	if (!postBody.j_name ||
			postBody.r_id.length !== 5 ||
			postBody.c_id.length !== 5) {
		//console.error('400: Parameter Error');
		res.send(400);
		return;
	}

	var route = postBody.r_id.substring(2, 5).toLowerCase(),
			scoreBonus = calculateBonus(postBody.c_score),
			scoreTop = calculateTop(postBody.c_score),
			queryConfig = {
				'text': 'UPDATE crimp_data SET ' +
								route + '_judge = $1, ' +
								route + '_raw = $2, ' +
								route + '_top = $3, ' +
								route + '_bonus = $4 WHERE ' +
								'c_id = $5 RETURNING '+
								route + '_raw;',
				'values': [postBody.j_name, postBody.c_score,
									 scoreTop, scoreBonus, postBody.c_id]
			};

	pg.connect(dbConn, function(err, client, done) {
		if (err) {
			console.error('500: Error fetching client from pool', err);
			res.send(500);
			return;
		}

		client.query(queryConfig, function(err, result) {
			// IMPORTANT! Release client back to pool
			done();

			if (err) {
				//console.error('400: Error running query', err);
				res.send(400);
			} else if (result.rowCount === 0) {
				//console.error('404: Data not found');
				res.send(404);
			} else {
				if (result.rowCount === 1 &&
						result.rows[0][route + '_raw'] === postBody.c_score) {
					console.log('Updated score: ' + postBody.c_id + '/' +
											postBody.c_score + ' by ' + postBody.j_name );

					//res.send(200, {});

					// Simulating latency
					setTimeout(function(){res.send(200, {})}, 5000);
				}
			}
		});
	});
});


app.get('/client/get/:round', function (req, res) {
	res.set('Content-Type', 'application/json');

	if (req.params.round.length !== 3) {
		//console.error('400: Parameter Error');
		res.send(400);
		return;
	}

	var	queryConfigTop = {},
			queryConfigBonus = {},
			queryState = {'qTop': false, 'qBonus': false},
			resultsTop,
			resultsBonus;

	if (req.params.round.substring(2,3) === 'Q') {
		queryConfigTop = {
			'text': 'SELECT c_id, ' +
							'q01_top, q02_top, q03_top, q04_top, q05_top, q06_top ' +
							'FROM crimp_data ' +
							'WHERE c_category = $1;',
			'values': [req.params.round]
		};
		queryConfigBonus = {
			'text': 'SELECT c_id, ' +
							'q01_bonus, q02_bonus, q03_bonus, q04_bonus, ' +
							'q05_bonus, q06_bonus ' +
							'FROM crimp_data ' +
							'WHERE c_category = $1;',
			'values': [req.params.round]
		};
	} else if (req.params.round.substring(2,3) === 'F') {
		queryConfigTop = {
			'text': 'SELECT c_id, ' +
							'f01_top, f02_top, f03_top, f04_top ' +
							'FROM crimp_data ' +
							'WHERE c_category = $1;',
			'values': [req.params.round]
		};
		queryConfigBonus = {
			'text': 'SELECT c_id, ' +
							'f01_bonus, f02_bonus, f03_bonus, f04_bonus ' +
							'FROM crimp_data ' +
							'WHERE c_category = $1;',
			'values': [req.params.round]
		};
	} else {
		res.send(400);
		return;
	}

	pg.connect(dbConn, function(err, client, done) {
		if (err) {
			console.error('500: Error fetching client from pool', err);
			res.send(500);
			return;
		}

		client.query(queryConfigTop, function(err, result) {
			// IMPORTANT! Release client back to pool
			done();
			queryState.qTop = true;

			if (err) {
				//console.error('400: Error running query', err);
				res.status(400);
			} else if (result.rowCount === 0) {
				//console.error('404: Data not found');
				res.status(404);
			} else {
				resultsTop = result.rows;
				//console.log('TOP: ');
				//console.log(resultsTop);
			}

			if (resultsTop && resultsBonus) {
				calculateClimberScore(resultsTop, resultsBonus, res);
			} else if (queryState.qTop && queryState.qBonus) {
				res.send(res.statusCode);
			}
		});

		client.query(queryConfigBonus, function(err, result) {
			// IMPORTANT! Release client back to pool
			done();
			queryState.qBonus = true;

			if (err) {
				//console.error('400: Error running query', err);
				res.status(400);
			} else if (result.rowCount === 0) {
				//console.error('404: Data not found');
				res.status(404);
			} else {
				resultsBonus = result.rows;
				//console.log('BONUS: ');
				//console.log(resultsBonus);
			}

			if (resultsTop && resultsBonus) {
				calculateClimberScore(resultsTop, resultsBonus, res);
			} else if (queryState.qTop && queryState.qBonus) {
				res.send(res.statusCode);
			}
		});
	});
});



var serverPort = Number (process.env.PORT || config.development.judge_port);
var server = app.listen(serverPort, function() {
  console.log('Listening on port %d', server.address().port);
});


function calculateTop (rawScore) {
	var i = 0;
	for (i; i < rawScore.length; i++) {
		if (rawScore[i] === 'T')
			return i+1;
	}
	return 0;
}


function calculateBonus (rawScore) {
	var i = 0;
	for (i; i < rawScore.length; i++) {
		if (rawScore[i] === 'T' || rawScore[i] === 'B')
			return i+1;
	}
	return 0;
}

function calculateClimberScore (resultsTop, resultsBonus, res) {
	var i = 0,
			message = {'climbers':[]};
	for (; i < resultsTop.length; i++) {
		var climber = {'c_id': '',
								   'top': 0,
									 't_att': 0,
									 'bonus': 0,
									 'b_att': 0 };

		climber.c_id = resultsTop[i].c_id;
		delete resultsTop[i].c_id;
		delete resultsBonus[i].c_id;

		for (var prop in resultsTop[i]) {
			if (resultsTop[i][prop]) {
				climber.top++;
				climber.t_att += resultsTop[i][prop];
			}
		}
		for (var prop in resultsBonus[i]) {
			if (resultsBonus[i][prop]) {
				climber.bonus++;
				climber.b_att += resultsBonus[i][prop];
			}
		}
		message.climbers.push(climber);
	}

	//console.log(message);
	res.send(200, JSON.stringify(message));
}