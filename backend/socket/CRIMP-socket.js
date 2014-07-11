var pg = require('pg'),
		websocketserver = require('ws').Server,
		config = require('./config.js');

// Setting development and production variables
var dbConn = process.env.DATABASE_URL || config.development.db_conn,
		serverPort = Number (process.env.PORT || config.development.port),
		socketAuth = config.production.socket_auth || config.development.socket_auth;

var wss = new websocketserver({port: serverPort});
wss._server._maxListeners = 0;
console.log('Listening on port %d', wss.options.port);

var activeClimbers = {
	'1': {'c_id':'', 'c_name':''},
	'2': {'c_id':'', 'c_name':''},
	'3': {'c_id':'', 'c_name':''},
	'4': {'c_id':'', 'c_name':''},
	'5': {'c_id':'', 'c_name':''},
	'6': {'c_id':'', 'c_name':''}
}


wss.on('connection', function (ws) {
	console.log('+1 ws client: ' + this.clients.length);

  ws.on('message', function (message) {
		try {
			message = JSON.parse(message);
			parseMessage(ws, message);
		} catch (e) {
			console.error('Error: Invalid JSON', e);
		}
  });

  ws.on('close', function () {
  	console.log('-1 ws client: ' + wss.clients.length);
  });
});

wss.on('close', function() {
  console.log('wss closed!');
});

wss.on('error', function() {
	console.log('wss error!');
});


wss.broadcast = function(ws, message) {
	//console.log('Broadcasting a message...');
	message = JSON.stringify(message);
  for (var i in this.clients) {
		if (this.clients[i] != ws)
			this.clients[i].send(message);
	}
};


function parseMessage(ws, message) {
	if (message.action === 'PING') {
		if (message.source === 'CRIMP-server') {
			console.log('CRIMP-server ping!');
		}
		return;
	}

	if (message.action === 'GET') {
		getLatestState(ws, message.data);
	} else if (message.auth_code === socketAuth) {
		switch (message.action) {
			case 'POST':
				broadcastNewScore(ws, message.data);
				break;
			case 'PUSH':
				setActiveClimber(ws, message.data);
				break;
			case 'POP':
				removeActiveClimber(ws, message.data);
				break;
			default:
				console.error('Error: Action \'' + message.action +
					'\' is not recognized');
				break;
		}
	} else {
		console.log('Unauthorized message received and discarded');
	}
}

/**
 *	Message = {
 *		'c_category'
 *	}
 */
function getLatestState(ws, message) {
	if (message.c_category.length !== 3) {
		return;
	}

	var	queryConfigTop = {},
			queryConfigBonus = {},
			queryState = {'qTop': false, 'qBonus': false},
			resultsTop,
			resultsBonus;

	if (message.c_category.substring(2,3) === 'Q') {
		queryConfigTop = {
			'text': 'SELECT c_id, c_name, ' +
							'q01_top, q02_top, q03_top, q04_top, q05_top, q06_top ' +
							'FROM crimp_data ' +
							'WHERE c_category = $1;',
			'values': [message.c_category]
		};
		queryConfigBonus = {
			'text': 'SELECT c_id, ' +
							'q01_bonus, q02_bonus, q03_bonus, q04_bonus, ' +
							'q05_bonus, q06_bonus ' +
							'FROM crimp_data ' +
							'WHERE c_category = $1;',
			'values': [message.c_category]
		};
	} else if (message.c_category.substring(2,3) === 'F') {
		queryConfigTop = {
			'text': 'SELECT c_id, c_name, ' +
							'f01_top, f02_top, f03_top, f04_top ' +
							'FROM crimp_data ' +
							'WHERE c_category = $1;',
			'values': [message.c_category]
		};
		queryConfigBonus = {
			'text': 'SELECT c_id, ' +
							'f01_bonus, f02_bonus, f03_bonus, f04_bonus ' +
							'FROM crimp_data ' +
							'WHERE c_category = $1;',
			'values': [message.c_category]
		};
	} else {
		console.error('Error: c_category = ' + message.c_category + '. Request dropped');
		return;
	}

	pg.connect(dbConn, function (err, client, done) {
		if (err) {
			console.error('500: Error fetching client from pool', err);
			return;
		}

		client.query(queryConfigTop, function (err, result) {
			// IMPORTANT! Release client back to pool
			done();
			queryState.qTop = true;

			if (err) {
				//console.error('400: Error running query', err);
				return;
			} else if (result.rowCount === 0) {
				console.error('GET 404: \'' + message.c_category +
					' Top\' data not found');
				return;
			} else {
				resultsTop = result.rows;
				//console.log(resultsTop);
			}

			if (resultsTop && resultsBonus) {
				tabulateAndSendScores(resultsTop, resultsBonus, ws);
			} else if (queryState.qTop && queryState.qBonus) {
				return;
			}
		});

		client.query(queryConfigBonus, function (err, result) {
			// IMPORTANT! Release client back to pool
			done();
			queryState.qBonus = true;

			if (err) {
				//console.error('400: Error running query', err);
				return;
			} else if (result.rowCount === 0) {
				console.error('GET 404: \'' + message.c_category +
					' Bonus\' data not found');
				return;
			} else {
				resultsBonus = result.rows;
				//console.log(resultsBonus);
			}

			if (resultsTop && resultsBonus) {
				tabulateAndSendScores(resultsTop, resultsBonus, ws);
			} else if (queryState.qTop && queryState.qBonus) {
				return;
			}
		});
	});
}


/**
 *	Message = {
 *		'c_id'
 *		'r_id'
 *		'top'
 *		'bonus'
 *	}
 */
function broadcastNewScore(ws, message) {
	var outgoingMessage = {
		'type': 'POST',
		'data': {
			'c_id': message.c_id,
			'tops': {},
			'bonus': {}
		}
	},
	route = message.r_id.substring(4,5);

	// message.top comes from CRIMP-server, do not change to message.tops
	outgoingMessage.data.tops[route] = message.top;
	outgoingMessage.data.bonus[route] = message.bonus;

	wss.broadcast(ws, outgoingMessage);
}


/**
 *	Message = {
 *		'c_id'
 *		'r_id'
 *	}
 */
function setActiveClimber(ws, message) {
	if (!message ||
			!message.c_id ||
			!message.r_id ||
			message.c_id.length !== 5 ||
			message.r_id.length !== 5 ||
			message.c_id.substring(0,2) !== message.r_id.substring(0,2) ||
			!(/^[a-z]+$/i.test(message.r_id.substring(2,3))) ){
		return;
	}

	// For reliablilty, CRIMP android app will send 3 requests to update
	// the activeClimber. This discards the excess requests.
	if (activeClimbers[message.r_id.substring(4, 5)] === message.c_id) return;

	var queryConfig = {
				'text': 'SELECT c_name FROM crimp_data WHERE c_id = $1;',
				'values': [message.c_id]
			},
			outgoingMessage = {
				'type': 'UPDATE',
				'data': null
			};

	pg.connect(dbConn, function (err, client, done) {
		if (err) {
			console.error('500: Error fetching client from pool', err);
			return;
		}

		client.query(queryConfig, function (err, result) {
			// IMPORTANT! Release client back to pool
			done();

			if (err) {
				//console.error('400: Error running query', err);
			} else if (result.rowCount !== 1) {
				console.error('Error activeClimber: ' + message.c_id + ' does not exists in database');
			} else if (result.rowCount === 1) {
				// Confirmed that climber exists in database
				activeClimbers[message.r_id.substring(4, 5)].c_id = message.c_id;
				activeClimbers[message.r_id.substring(4, 5)].c_name = result.rows[0].c_name;

				outgoingMessage.data = activeClimbers;
				wss.broadcast(ws, outgoingMessage);

				console.log(JSON.stringify(activeClimbers));
				setTimeout(function() {
					removeActiveClimber(null, message);
				}, 240000);
			}
		});
	});
}


/**
 *	Message = {
 *		'c_id'
 *		'r_id'
 *	}
 */
function removeActiveClimber(ws, message) {
	if (!message || !message.c_id || !message.r_id) return;

	var outgoingMessage = {
		'type': 'UPDATE',
		'data': null
	};

	if (activeClimbers[message.r_id.substring(4, 5)].c_id === message.c_id) {
		activeClimbers[message.r_id.substring(4, 5)].c_id = '';
		activeClimbers[message.r_id.substring(4, 5)].c_name = '';

		outgoingMessage.data = activeClimbers;
		wss.broadcast(ws, outgoingMessage);

		console.log(JSON.stringify(activeClimbers));
	} else {
		//console.log('Error activeClimber: ' + message.c_id +
		//	' is not active on ' + message.r_id)
	}
}


function tabulateAndSendScores (resultsTop, resultsBonus, ws) {
	var i = 0,
			outgoingMessage = {
				'type': 'GET',
				'data': {
					'activeClimbers': activeClimbers,
					'climbers':[]
				}
			};

	for (; i < resultsTop.length; i++) {
		if (resultsTop[i].c_id === resultsBonus[i].c_id) {
			var climber = {
				'c_id': '',
				'c_name': '',
			 	'tops': {},
			 	'bonus': {}
			};

			climber.c_id = resultsTop[i].c_id;
			climber.c_name = resultsTop[i].c_name;
			delete resultsTop[i].c_id;
			delete resultsTop[i].c_name;
			delete resultsBonus[i].c_id;

			var j = 1;
			for (var prop in resultsTop[i]) {
				if (resultsTop[i][prop]) {
					climber.tops[j] = resultsTop[i][prop];
				} else {
					// turns 'undefined' and 'null' to 0
					climber.tops[j] = 0;
				}
				j++;
			}

			j = 1;
			for (var prop in resultsBonus[i]) {
				if (resultsBonus[i][prop]) {
					climber.bonus[j] = resultsBonus[i][prop];
				} else {
					// turns 'undefined' and 'null' to 0
					climber.bonus[j] = 0;
				}
				j++;
			}
			outgoingMessage.data.climbers.push(climber);
		}
	}

	//console.log(JSON.stringify(outgoingMessage, null, 2));
	ws.send(JSON.stringify(outgoingMessage));
}