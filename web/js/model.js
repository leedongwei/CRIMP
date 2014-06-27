var ws;
restartWebsocket();

function restartWebsocket() {
	if (ws)	ws.close();
	ws = new WebSocket(config.socketHost);

	ws.onopen = function(event) {
		console.log('Opened connection to ' + config.socketHost);
		ws.send('{"action":"GET", "data":{"c_category":"NMQ"}}');
		pingSocket();
	}


	ws.onclose = function(event) {
		console.log('Closed connection to ' + config.socketHost);

		// CHANGE TO ALERT
		//console.log('Disconnected from server! Refresh to reconnect!.');
	}


	ws.onmessage = function(event) {
		console.log(event.data);
	}


	ws.onerror = function(event) {
		console.error('Error ' + config.socketHost + ': ', event);
	}
}

// Recursively send a ping message to CRIMP-socket every 45s
function pingSocket() {
	console.log('Pinging CRIMP-socket!');
	ws.send(JSON.stringify({'action':'PING', 'source':'CRIMP-server'}), function (error) {
		if (error) console.error('ws.send(ping) ' + error);
	});
	setTimeout(function() {
		if (ws.readyState === 1)	pingSocket();
	}, 30000);
};