var bodyParser = require('body-parser'),
		express = require('express'),
		pg = require('pg'),
		http = require('http'),
		websocket = require('ws');
		config = require('./config.js');

// Setting development and production variables
var dbConn = process.env.DATABASE_URL || config.development.db_conn,
		serverPort = Number (process.env.PORT || config.development.port),
		serverAuth = config.production.auth_code || config.development.auth_code,
		socketAuth = config.production.socket_auth || config.development.socket_auth,
		socketHost = config.production.socketserver || config.development.socketserver;


var app = express(),
		server = app.listen(serverPort, function() {
 			console.log('Listening on port %d', server.address().port);
		}),
		ws = new websocket('ws://' + socketHost);
app.use(bodyParser());


app.get('/judge/get/:c_category', function (req, res) {
	res.set('Content-Type', 'application/json');

	if (req.params.c_category.length !== 3) {
		//console.error('400: Parameter Error');
		res.send(400);
		return;
	}

	var	message = {'climbers':[]},
			queryConfig = {
				'text': 'SELECT c_id, c_name FROM crimp_data ' +
								'WHERE c_category = $1;',
				'values': [req.params.c_category]
			};

	pg.connect(dbConn, function (err, client, done) {
		if (err) {
			console.error('500: Error fetching client from pool', err);
			res.send(500);
			return;
		}

		client.query(queryConfig, function (err, result) {
			// IMPORTANT! Release client back to pool
			done();

			if (err) {
				//console.error('400: Error running query', err);
				res.send(400);;
			} else if (result.rowCount === 0) {
				//console.error('404: Data not found');
				res.send(404);
			} else {
				result.rows.forEach(function (entry) {
					message.climbers.push({
						'c_id': entry.c_id, 'c_name': entry.c_name
					});
				});

				res.send(200, JSON.stringify(message));

				// Simulating latency
				//setTimeout(function(){res.send(200, JSON.stringify(message))}, 5000);
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
			!(/^[a-z]+$/i.test(r_id.substring(2,3))) ){
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

	pg.connect(dbConn, function (err, client, done) {
		if (err) {
			console.error('500: Error fetching client from pool', err);
			res.send(500);
			return;
		}

		client.query(queryConfig, function (err, result) {
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

				res.send(200, JSON.stringify(message));

				// Simulating latency
				//setTimeout(function(){res.send(200, JSON.stringify(message))}, 5000);
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

	if (postBody.auth_code !== serverAuth) {
		console.error('401 Unauthorized: ' + postBody.j_name);
		res.send(401);
		return;
	}

	if (!postBody.j_name ||
			!postBody.c_id ||
			!postBody.r_id ||
			postBody.c_score === undefined ||
			postBody.c_score === null ||
			postBody.c_id.length !== 5 ||
			postBody.r_id.length !== 5 ||
			postBody.c_id.substring(0,2) !== postBody.r_id.substring(0,2) ||
			!(/^[a-z]+$/i.test(postBody.r_id.substring(2,3))) ) {
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

	pg.connect(dbConn, function (err, client, done) {
		if (err) {
			console.error('500: Error fetching client from pool', err);
			res.send(500);
			return;
		}

		client.query(queryConfig, function (err, result) {
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

					res.send(200, {});

					// Send to CRIMP-socket to update web
					var postData = {
						'c_id': postBody.c_id,
						'r_id': postBody.r_id,
						'top': scoreTop,
						'bonus': scoreBonus
					};
					sendToCrimpSocket('POST', postData);


					// Simulating latency
					//setTimeout(function(){res.send(200, {})}, 5000);
				}
			}
		});
	});
});


app.get('/judge/push/:c_id/:r_id', function (req, res) {
	// Respond to android app immediately
	res.send(200);
	setClimberOnWall('PUSH', req.params);
});


app.get('/judge/pop/:c_id/:r_id', function (req, res) {
	// Respond to android app immediately
	res.send(200);
	setClimberOnWall('POP', req.params);
});


app.get('/admin/get/:r_id', function (req, res) {
	var r_id = req.params.r_id;
	res.set('Content-Type', 'application/json');

	if (r_id.length !== 5 ||
			!(/^[a-z]+$/i.test(r_id.substring(2,3))) ){
		//console.error('400: Parameter Error');
		res.send(400);
		return;
	}

	var r_id = req.params.r_id;
			c_category = r_id.substring(0,3),
			route = r_id.substring(2, 5).toLowerCase() + '_raw',
			judge = r_id.substring(2, 5).toLowerCase() + '_judge',
			message = {	'climbers': []},
			queryConfig = {
				'text': 'SELECT c_id, ' + judge + ', c_name, ' + route +
								' FROM crimp_data ' +
								'WHERE c_category = $1;',
				'values': [c_category]
			};

	pg.connect(dbConn, function (err, client, done) {
		if (err) {
			console.error('500: Error fetching client from pool', err);
			res.send(500);
			return;
		}

		client.query(queryConfig, function (err, result) {
			// IMPORTANT! Release client back to pool
			done();

			if (err) {
				//console.error('400: Error running query', err);
				res.send(400);
			} else if (result.rowCount === 0) {
				//console.error('404: Data not found');
				res.send(404);
			} else {

				result.rows.forEach(function (entry) {
					message.climbers.push(entry);
				});

				res.send(200, JSON.stringify(message, null, 2));
			}
		});
	});
});


// TODO: Reopen ws to CRIMP-socket
app.get('/admin/open', function (req, res) {
	res.send(200);
	//restartWebsocket();
});


ws.on('open', function() {
  console.log('Opened connection to ws://' + socketHost);
});


ws.on('close', function() {
  console.log('Closed connection to ws://' + socketHost);
  //restartWebsocket();
});


ws.on('message', function(data, flags) {
	// CRIMP-server should never receive any messages from CRIMP-socket!
	console.log('Message received from ws://' + socketHost +':');
	console.log(data);
});


ws.on('error', function() {
	console.log('Error on ws://' + socketHost);
	console.log(JSON.stringify(ws, null, 2));
	//restartWebsocket();
});


//app.post('/judge/set')
function calculateTop(rawScore) {
	var i = 0;
	for (i; i < rawScore.length; i++) {
		if (rawScore[i] === 'T')
			return i+1;
	}
	return 0;
}

//app.post('/judge/set')
function calculateBonus(rawScore) {
	var i = 0;
	for (i; i < rawScore.length; i++) {
		if (rawScore[i] === 'T' || rawScore[i] === 'B')
			return i+1;
	}
	return 0;
}

function restartWebsocket() {
	ws = new websocket('ws://' + socketHost);
}

//app.get('/judge/push/' & '/judge/pop/')
function setClimberOnWall(action, data) {
	var c_id = data.c_id,
			r_id = data.r_id;
	console.log('OnWall ' + action + ': '+ c_id + '    ' + r_id);

	if (c_id.length !== 5 ||
			r_id.length !== 5 ||
			c_id.substring(0,2) !== r_id.substring(0,2) ||
			!(/^[a-z]+$/i.test(r_id.substring(2,3))) ){
		// parameter error, do not send to socket
		return;
	} else {
		sendToCrimpSocket(action, data);
	}
}

function sendToCrimpSocket(action, data) {
	// actions recognized by CRIMP-socket: POST, PUSH, POP
	var message = {
		'action': action,
		'auth_code': socketAuth,
		'data': data
	};

	//console.log(JSON.stringify(message, null, 2));
	ws.send(JSON.stringify(message), function (error) {
		if (error) console.log(error);
	});
}