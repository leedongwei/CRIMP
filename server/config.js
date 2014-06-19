module.exports = {

	'development': {
		'host':'127.0.0.1',
		'judge_port':'3000',
		'client_port':'8080',
		'auth_code': 'crimpAUTH',
		'db_conn':'postgres://crimp:123456@localhost/crimp-db'
	},

	// production: use Heroku's process.env
	'production': {
		'auth_code': ''
	}
}