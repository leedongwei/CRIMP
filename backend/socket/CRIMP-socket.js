var bodyParser = require('body-parser'),
		express = require('express'),
		pg = require('pg'),
		websocket = require('ws'),
		config = require('./config.js');

var app = express();
app.use(bodyParser());

var dbConn = process.env.DATABASE_URL || config.development.db_conn;

app.post('/server/push', function (req, res) {
	// Respond to server immediately
	res.send(200);

	var postBody = req.body;

});


app.get('/judge/push/:c_id/:r_id', function (req, res) {
	// Respond to android app immediately
	res.send(200);

});


app.get('/judge/pop/:c_id/:r_id', function (req, res) {
	// Respond to android app immediately
	res.send(200);

});


app.get('/client/get/:c_category', function (req, res) {
	res.set('Content-Type', 'application/json');

	if (req.params.c_category.length !== 3) {
		//console.error('400: Parameter Error');
		res.send(400);
		return;
	}

	var	queryConfigTop = {},
			queryConfigBonus = {},
			queryState = {'qTop': false, 'qBonus': false},
			resultsTop,
			resultsBonus;

	if (req.params.c_category.substring(2,3) === 'Q') {
		queryConfigTop = {
			'text': 'SELECT c_id, ' +
							'q01_top, q02_top, q03_top, q04_top, q05_top, q06_top ' +
							'FROM crimp_data ' +
							'WHERE c_category = $1;',
			'values': [req.params.c_category]
		};
		queryConfigBonus = {
			'text': 'SELECT c_id, ' +
							'q01_bonus, q02_bonus, q03_bonus, q04_bonus, ' +
							'q05_bonus, q06_bonus ' +
							'FROM crimp_data ' +
							'WHERE c_category = $1;',
			'values': [req.params.c_category]
		};
	} else if (req.params.c_category.substring(2,3) === 'F') {
		queryConfigTop = {
			'text': 'SELECT c_id, ' +
							'f01_top, f02_top, f03_top, f04_top ' +
							'FROM crimp_data ' +
							'WHERE c_category = $1;',
			'values': [req.params.c_category]
		};
		queryConfigBonus = {
			'text': 'SELECT c_id, ' +
							'f01_bonus, f02_bonus, f03_bonus, f04_bonus ' +
							'FROM crimp_data ' +
							'WHERE c_category = $1;',
			'values': [req.params.c_category]
		};
	} else {
		res.send(400);
		return;
	}

	pg.connect(dbConn, function (err, client, done) {
		if (err) {
			console.error('500: Error fetching client from pool', err);
			res.send(500);
			return;
		}

		client.query(queryConfigTop, function (err, result) {
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
				console.log(resultsTop);
			}

			if (resultsTop && resultsBonus) {
				calculateClimberScore(resultsTop, resultsBonus, res);
			} else if (queryState.qTop && queryState.qBonus) {
				res.send(res.statusCode);
			}
		});

		client.query(queryConfigBonus, function (err, result) {
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
				console.log(resultsBonus);
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
  console.log(process.env);
});


function calculateClimberScore (resultsTop, resultsBonus, res) {
	var i = 0,
			message = {'climbers':[]};
	for (; i < resultsTop.length; i++) {
		if (resultsTop[i].c_id === resultsBonus[i].c_id) {
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
	}

	//console.log(message);
	res.send(200, JSON.stringify(message));
}