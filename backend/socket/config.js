module.exports = {

	'development': {
		'host':'127.0.0.1',
		'port':'8080',
		'socket_auth':'socketAUTH',
		'db_conn':'postgres://crimp:123456@localhost/crimp-db'
	},

	// production: use Heroku's process.env
	'production': {
		//'host': Heroku
		//'port': Heroku
		'socket_auth':null,
		'db_conn':null
	}
}