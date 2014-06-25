module.exports = {

	'development': {
		'host':'127.0.0.1',
		'port':'8080',
		'db_conn':'postgres://crimp:123456@localhost/crimp-db'
	},

	// production: use Heroku's process.env
	'production': {
		'auth_code':'',
		'db_conn':''
	}
}