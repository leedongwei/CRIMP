module.exports = {

	'development': {
		'host':'127.0.0.1',
		'port':'3000',
		'auth_code': 'crimpAUTH',
		'db_conn':'postgres://crimp:123456@localhost/crimp-db',
		'socketserver':'127.0.0.1:8080',
		'socket_auth':'socketAUTH'
	},

	// production: use Heroku's process.env
	'production': {
		//'host': Heroku
		//'port': Heroku
		'auth_code':null,
		//'db_conn': Heroku
		'socketserver':null,
		'socket_auth':null
	}
}