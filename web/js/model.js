var config = {
	'socketHost':'ws://127.0.0.1:8080'
};

var ws;
restartWebsocket();

var activeClimbers = {
	'1': {'c_id':'', 'c_name':''},
	'2': {'c_id':'', 'c_name':''},
	'3': {'c_id':'', 'c_name':''},
	'4': {'c_id':'', 'c_name':''},
	'5': {'c_id':'', 'c_name':''},
	'6': {'c_id':'', 'c_name':''}
},
ClimberListView = [],
ClimberListNew = [];


function restartWebsocket() {
	if (ws)	ws.close();
	ws = new WebSocket(config.socketHost);

	ws.onopen = function(event) {
		console.log('Opened connection to ' + config.socketHost);
		ws.send('{"action":"GET", "data":{"c_category":"NWQ"}}');
		pingSocket();
	}

	ws.onclose = function(event) {
		console.log('Closed connection to ' + config.socketHost);

		// CHANGE TO ALERT TO WARN USERS
		//console.log('Disconnected from server! Refresh to reconnect!');
	}

	ws.onmessage = function(event) {
		console.log(event.data);
		parseData(JSON.parse(event.data));
	}

	ws.onerror = function(event) {
		console.error('Error ' + config.socketHost + ': ', event);
	}
}

// Recursively send a ping message to CRIMP-socket every 45s
function pingSocket() {
	console.log('Pinging CRIMP-socket!');
	ws.send(JSON.stringify({'action':'PING', 'source':'CRIMP-client'}));
	setTimeout(function() {
		if (ws.readyState === 1)	pingSocket();
	}, 10000);
};

function parseData(incomingMessage) {
	switch (incomingMessage.type) {
		case ('GET'):
			setLocalScores(incomingMessage.data);
			break;
		case ('POST'):
			updateLocalScores(incomingMessage.data);
			break;
		case ('UPDATE'):
			updateActiveClimber(incomingMessage.data);
			break;
		default:
			console.error('Error: Action \'' + incomingMessage.action +
				'\' is not recognized');
			break;
	}
}

function setLocalScores(data) {
	console.log('set');
	if (!data.climbers || !data.activeClimbers)	return;

	updateActiveClimber(data.activeClimbers);

	// Load server data into local array
	for (var i = data.climbers.length - 1; i >= 0; i--) {
		ClimberListView.push(initializeAClimber(data.climbers[i]));
	};

	// Sort the array, then render to view
	ClimberListView.sort(climberSort);
	renderListView();
}

function updateLocalScores(data) {
	console.log('update');
}

function updateActiveClimber(data) {
	console.log('AC');
	for (var prop in data) {
		if (!data[prop].c_id) {
			$('#active' + prop + ' > .activeId').html('Empty!');
			$('#active' + prop + ' > .activeName').html('');
		} else if (data[prop].c_id !== activeClimbers[prop].c_id) {
			activeClimbers[prop] = data[prop];
			$('#active' + prop + ' > .activeId').html(activeClimbers[prop].c_id);
			$('#active' + prop + ' > .activeName').html(activeClimbers[prop].c_name);
		}
	}

	console.log(JSON.stringify(activeClimbers));
}

function initializeAClimber(data) {
	var tops = 0, topAtt = 0, bonus = 0, bonusAtt = 0;

	for (var prop in data.tops) {
		if (data.tops[prop]) tops++;
		topAtt += data.tops[prop];
	}
	for (var prop in data.bonus) {
		if (data.bonus[prop]) bonus++;
		bonusAtt += data.bonus[prop];
	}

	return {
		'c_rank': '',
		'c_id': data.c_id,
		'tops': {
			'1': data.tops['1'],
			'2': data.tops['2'],
			'3': data.tops['3'],
			'4': data.tops['4'],
			'5': data.tops['5'],
			'6': data.tops['6']
		},
		'bonus': {
			'1': data.bonus['1'],
			'2': data.bonus['2'],
			'3': data.bonus['3'],
			'4': data.bonus['4'],
			'5': data.bonus['5'],
			'6': data.bonus['6']
		},
		'total': {
			'tops': tops,
			'topAttempts': topAtt,
			'bonus': bonus,
			'bonusAttempts': bonusAtt
		}
	};
}

function climberSort(a, b) {
	if (a.total.tops === b.total.tops) {
		if (a.total.topAttempts === b.total.topAttempts) {
			if (a.total.bonus === b.total.bonus) {
				if (a.total.bonusAttempts === b.total.bonusAttempts) {
					return 0;
				}
				return a.total.bonusAttempts < b.total.bonusAttempts ? -1 : 1;
			}
			return a.total.bonus > b.total.bonus ? -1 : 1;
		}
		return a.total.topAttempts < b.total.topAttempts ? -1 : 1;
	}
	return a.total.tops > b.total.tops ? -1 : 1;
}

function renderListView() {
	for (var i = 0; i < ClimberListView.length; i++) {
		var id = '#' + ClimberListView[i].c_id;

		// Current rank
		$(id + ' > .c_rank').html(i+1);

		// Overal scores
		$(id + ' > .c_top > .top')
			.html(ClimberListView[i].total.tops);
		$(id + ' > .c_top > .topAttempts')
			.html(ClimberListView[i].total.topAttempts);
		$(id + ' > .c_bonus > .bonus')
			.html(ClimberListView[i].total.bonus);
		$(id + ' > .c_bonus > .bonusAttempts')
			.html(ClimberListView[i].total.bonusAttempts);

		// Scores for each routes
		$(id + ' > .c_score > #route1 > .top')
			.html(ClimberListView[i].tops['1']);
		$(id + ' > .c_score > #route1 > .bonus')
			.html(ClimberListView[i].bonus['1']);
		$(id + ' > .c_score > #route2 > .top')
			.html(ClimberListView[i].tops['2']);
		$(id + ' > .c_score > #route2 > .bonus')
			.html(ClimberListView[i].bonus['2']);
		$(id + ' > .c_score > #route3 > .top')
			.html(ClimberListView[i].tops['3']);
		$(id + ' > .c_score > #route3 > .bonus')
			.html(ClimberListView[i].bonus['3']);
		$(id + ' > .c_score > #route4 > .top')
			.html(ClimberListView[i].tops['4']);
		$(id + ' > .c_score > #route4 > .bonus')
			.html(ClimberListView[i].bonus['4']);
		$(id + ' > .c_score > #route5 > .top')
			.html(ClimberListView[i].tops['5']);
		$(id + ' > .c_score > #route5 > .bonus')
			.html(ClimberListView[i].bonus['5']);
		$(id + ' > .c_score > #route6 > .top')
			.html(ClimberListView[i].tops['6']);
		$(id + ' > .c_score > #route6 > .bonus')
			.html(ClimberListView[i].bonus['6']);

	};
}