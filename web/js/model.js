var config = {
	'socketHost':'ws://obscure-plateau-socket.herokuapp.com/'
	//'socketHost': 'ws://127.0.0.1:8080'
};

var	activeClimbers = {
	'1': {'c_id':'', 'c_name':''},
	'2': {'c_id':'', 'c_name':''},
	'3': {'c_id':'', 'c_name':''},
	'4': {'c_id':'', 'c_name':''},
	'5': {'c_id':'', 'c_name':''},
	'6': {'c_id':'', 'c_name':''}
},
ClimberListView = [],
ws;

$('.scoreHeader > h2').html('Live Scores <span style="color:red;">(NOT CONNECTED)</span>');
restartWebsocket()

function restartWebsocket() {
	if (ws)	ws.close();
	ws = new WebSocket(config.socketHost);

	ws.onopen = function(event) {
		console.log('Opened connection to ' + config.socketHost);
		ws.send('{"action":"GET", "data":{"c_category": \"' + ws_category + '\"}}');
		pingSocket();

		$('.scoreHeader > h2').html('Live Scores <span id="ws_status" style="color:green;">(YOU ARE CONNECTED!)</span>');

		setTimeout(function () {
			$('#ws_status').animate({opacity: 'toggle'}, 2000);
		}, 3000);
	}

	ws.onclose = function(event) {
		console.log('Closed connection to ' + config.socketHost);
		$('.scoreHeader > h2').html('Live Scores <span style="color:red;">(NOT CONNECTED)</span>');
	}

	ws.onmessage = function(event) {
		parseData(JSON.parse(event.data));
	}

	ws.onerror = function(event) {
		console.error('Error ' + config.socketHost + ': ', event);
		$('.scoreHeader > h2').html('Live Scores <span style="color:red;">(NOT CONNECTED)</span>');
	}
}

// Recursively send a ping message to CRIMP-socket every 40s
function pingSocket() {
	ws.send(JSON.stringify({'action':'PING', 'source':'CRIMP-client'}));
	//console.log('Pinging server!');
	setTimeout(function() {
		if (ws.readyState === 1)	pingSocket();
	}, 40000);
};

function parseData(incomingMessage) {
	switch (incomingMessage.type) {
		case ('GET'):
			setLocalScores(incomingMessage.data);
			updateActiveClimber(incomingMessage.data.activeClimbers);
			break;
		case ('POST'):
			if (ws_category.substring(0,2) === incomingMessage.data.c_id.substring(0,2)) {
				updateLocalScores(incomingMessage.data);
			}
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
	if (!data.climbers || !data.activeClimbers)	return;

	// Load server data into local array
	for (var i = 0; i < data.climbers.length; i++) {
		ClimberListView.push(initializeAClimber(data.climbers[i]));
	};

	// Sort the array, then render to view
	ClimberListView.sort(climberSort);
	renderListView();
}

function updateLocalScores(data) {
	var updatedLocalScores = false,
			id = '#' + data.c_id;

	ClimberListView.every(function (climber){
		if (climber.c_id === data.c_id) {

			for (var prop in data.tops) {
				climber.tops[prop] = data.tops[prop];
			}

			for (var prop in data.bonus) {
				climber.bonus[prop] = data.bonus[prop];
			}

			var tops = 0, topAtt = 0,
					bonus = 0, bonusAtt = 0;
			for (var prop in climber.tops) {
				if (climber.tops[prop]) tops++;
				topAtt += climber.tops[prop];
			}
			for (var prop in climber.bonus) {
				if (climber.bonus[prop]) bonus++;
				bonusAtt += climber.bonus[prop];
			}

			climber.total = {
				'tops': tops,
				'topAttempts': topAtt,
				'bonus': bonus,
				'bonusAttempts': bonusAtt
			}
			updatedLocalScores = true;
		}

		if (updatedLocalScores) {
			return false;
		} else {
			return true;
		}
	});

	ClimberListView.sort(climberSort);
	renderListView();
}

function updateActiveClimber(data) {
	for (var prop in data) {
		if (data[prop].c_id === activeClimbers[prop].c_id) {
			// do nothing
		} else if (!data[prop].c_id) {
			activeClimbers[prop] = data[prop];
			$('#active' + prop + ' > .activeId').animate({width: 'toggle'}, 50);
			$('#active' + prop + ' > .activeName').animate({width: 'toggle'}, 50);
			$('#active' + prop + ' > .activeId').html('Empty Lane!');
			$('#active' + prop + ' > .activeName').html('');
			$('#active' + prop + ' > .activeId').animate({width: 'toggle'}, 300);
			$('#active' + prop + ' > .activeName').animate({width: 'toggle'}, 300);

		} else if (data[prop].c_id !== activeClimbers[prop].c_id) {
			activeClimbers[prop] = data[prop];
			$('#active' + prop + ' > .activeId').animate({width: 'toggle'}, 50);
			$('#active' + prop + ' > .activeName').animate({width: 'toggle'}, 50);
			$('#active' + prop + ' > .activeId').html(activeClimbers[prop].c_id);
			$('#active' + prop + ' > .activeName').html(activeClimbers[prop].c_name);
			$('#active' + prop + ' > .activeId').animate({width: 'toggle'}, 400);
			$('#active' + prop + ' > .activeName').animate({width: 'toggle'}, 400);
		}
	}
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
		'c_name': data.c_name,
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
					return a.c_id < b.c_id ? -1 : 1;
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
	var i = 1;
	ClimberListView.forEach(function (climber) {
		climber.c_rank = i;
		var stringBloc = '<div id="'+climber.c_id+'"><span class="c_rank">'+i+'</span><span class="c_id">'+climber.c_id+'</span><h3 class="c_name">'+climber.c_name+'</h3><span class="c_top badge">'+climber.total.tops+'t'+climber.total.topAttempts+'</span><span class="c_bonus badge">'+climber.total.bonus+'b'+climber.total.bonusAttempts+'</span><div class="c_score"><div class="c_score-col" id="route1">t'+climber.tops['1']+' b'+climber.bonus['1']+'</div><div class="c_score-col" id="route2">t'+climber.tops['2']+' b'+climber.bonus['2']+'</div><div class="c_score-col" id="route3">t'+climber.tops['3']+' b'+climber.bonus['3']+'</div><div class="c_score-col" id="route4">t'+climber.tops['4']+' b'+climber.bonus['4']+'</div><div class="c_score-col" id="route5">t'+climber.tops['5']+' b'+climber.bonus['5']+'</div><div class="c_score-col" id="route6">t'+climber.tops['6']+' b'+climber.bonus['6']+'</div></div></div>';

		$('#rank' + i).html(stringBloc);

		i++;
	})
}