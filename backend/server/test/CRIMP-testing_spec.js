var frisby = require('frisby');

var URL = 'http://localhost:3000',
		authCode = 'crimpAUTH';

/**
 * Testing GET /judge/get/:round
 */
frisby.create('Correct GET request')
  .get(URL + '/judge/get/NWQ')
	  .expectStatus(200)
	  .expectHeaderContains('Content-Type', 'application/json')
	  .expectJSONTypes({
	    climbers: [
	    	{
	    		c_id: String,
	    		c_name: String
	    	}
	    ]
	  })
.toss();

frisby.create('Wrong case')
  .get(URL + '/judge/get/nwq')
	  .expectStatus(404)
	  .expectHeaderContains('Content-Type', 'application/json')
.toss();

frisby.create('Correct category, wrong round')
  .get(URL + '/judge/get/NWA')
	  .expectStatus(404)
	  .expectHeaderContains('Content-Type', 'application/json')
.toss();

frisby.create('Wrong category, correct round')
  .get(URL + '/judge/get/ABQ')
	  .expectStatus(404)
	  .expectHeaderContains('Content-Type', 'application/json')
.toss();

frisby.create('Wrong category, wrong round')
  .get(URL + '/judge/get/ABC')
	  .expectStatus(404)
	  .expectHeaderContains('Content-Type', 'application/json')
.toss();

frisby.create('Short round name')
  .get(URL + '/judge/get/AB')
	  .expectStatus(400)
	  .expectHeaderContains('Content-Type', 'application/json')
.toss();

frisby.create('Long round name')
  .get(URL + '/judge/get/ABCD')
	  .expectStatus(400)
	  .expectHeaderContains('Content-Type', 'application/json')
.toss();

frisby.create('Correct round name, minus 1 character')
  .get(URL + '/judge/get/NW')
	  .expectStatus(400)
	  .expectHeaderContains('Content-Type', 'application/json')
.toss();

frisby.create('Correct round name, plus 1 character')
  .get(URL + '/judge/get/NWQ1')
	  .expectStatus(400)
	  .expectHeaderContains('Content-Type', 'application/json')
.toss();










/**
 * Testing GET /judge/get/:c_id/:r_id
 */
frisby.create('Correct GET request')
  .get(URL + '/judge/get/NM001/NMQ01')
	  .expectStatus(200)
	  .expectHeaderContains('Content-Type', 'application/json')
	  .expectJSONTypes({
	    c_name: String,
	    c_score: String
	  })
.toss();

frisby.create('climber does not exist')
  .get(URL + '/judge/get/NW200/NWQ02')
	  .expectStatus(404)
	  .expectHeaderContains('Content-Type', 'application/json')
.toss();

frisby.create('route does not exist')
  .get(URL + '/judge/get/NW002/NWx02')
	  .expectStatus(400)
	  .expectHeaderContains('Content-Type', 'application/json')
.toss();

frisby.create('both does not exist')
  .get(URL + '/judge/get/NW200/NWx02')
	  .expectStatus(400)
	  .expectHeaderContains('Content-Type', 'application/json')
.toss();

frisby.create('Shorter c_id, correct r_id')
  .get(URL + '/judge/get/NM01/NMQ01')
	  .expectStatus(400)
	  .expectHeaderContains('Content-Type', 'application/json')
.toss();

frisby.create('Longer c_id, correct r_id')
  .get(URL + '/judge/get/NM0001/NMQ01')
	  .expectStatus(400)
	  .expectHeaderContains('Content-Type', 'application/json')
.toss();

frisby.create('Correct c_id, shorter r_id')
  .get(URL + '/judge/get/NM001/NMQ1')
	  .expectStatus(400)
	  .expectHeaderContains('Content-Type', 'application/json')
.toss();

frisby.create('Correct c_id, longer r_id')
  .get(URL + '/judge/get/NM001/NMQ001')
	  .expectStatus(400)
	  .expectHeaderContains('Content-Type', 'application/json')
.toss();

frisby.create('substring(0,2) is not equal, category name is correct')
  .get(URL + '/judge/get/NM001/ABQ01')
	  .expectStatus(400)
	  .expectHeaderContains('Content-Type', 'application/json')
.toss();

frisby.create('substring(0,2) is not equal, category name begins with numeric char')
  .get(URL + '/judge/get/NM001/AB001')
	  .expectStatus(400)
	  .expectHeaderContains('Content-Type', 'application/json')
.toss();

frisby.create('substring(0,2) is not equal, category name begins with wrong alphabet char')
  .get(URL + '/judge/get/AB001/NWx01')
	  .expectStatus(400)
	  .expectHeaderContains('Content-Type', 'application/json')
.toss();

frisby.create('substring(0,2) is equal but non-existent, category name begins with numeric char')
  .get(URL + '/judge/get/AB001/AB001')
	  .expectStatus(400)
	  .expectHeaderContains('Content-Type', 'application/json')
.toss();

frisby.create('substring(0,2) is equal but non-existent, category name begins with wrong alphabet char')
  .get(URL + '/judge/get/AB001/ABx01')
	  .expectStatus(400)
	  .expectHeaderContains('Content-Type', 'application/json')
.toss();

frisby.create('substring(0,2) is equal but non-existent, category name is correct')
  .get(URL + '/judge/get/AB001/ABQ01')
	  .expectStatus(404)
	  .expectHeaderContains('Content-Type', 'application/json')
.toss();

frisby.create('substring(0,2) is equal and correct, but category name begins with numeric char')
  .get(URL + '/judge/get/NW001/NW001')
	  .expectStatus(400)
	  .expectHeaderContains('Content-Type', 'application/json')
.toss();

frisby.create('substring(0,2) is equal and correct, but category name begins with wrong alphabet char')
  .get(URL + '/judge/get/NW001/NWx01')
	  .expectStatus(400)
	  .expectHeaderContains('Content-Type', 'application/json')
.toss();










/**
 * Testing POST /judge/set
 */
frisby.create('Correct POST request')
  .post(URL + '/judge/set', {
    j_name: 'DongWei',
		auth_code: authCode,
		r_id: 'NWQ01',
		c_id: 'NW001',
		c_score: '111BT'
  }, {json: true})
  	.expectStatus(200)
    .expectHeaderContains('Content-Type', 'json')
.toss();

frisby.create('No j_name')
  .post(URL + '/judge/set', {
    j_name: '',
		auth_code: authCode,
		r_id: 'NWQ01',
		c_id: 'NW001',
		c_score: '111BT'
  }, {json: true})
  	.expectStatus(400)
    .expectHeaderContains('Content-Type', 'json')
.toss();

frisby.create('No auth_code')
  .post(URL + '/judge/set', {
    j_name: 'DongWei',
		r_id: 'NWQ01',
		c_id: 'NW001',
		c_score: '111BT'
  }, {json: true})
  	.expectStatus(401)
    .expectHeaderContains('Content-Type', 'json')
.toss();


frisby.create('Wrong auth_code')
  .post(URL + '/judge/set', {
    j_name: 'DongWei',
		auth_code: 'this is a wrong auth_code',
		r_id: 'NWQ01',
		c_id: 'NW001',
		c_score: '111BT'
  }, {json: true})
  	.expectStatus(401)
    .expectHeaderContains('Content-Type', 'json')
.toss();

frisby.create('No c_score')
  .post(URL + '/judge/set', {
    j_name: 'DongWei',
		auth_code: authCode,
		r_id: 'NWQ01',
		c_id: 'NW001'
  }, {json: true})
  	.expectStatus(400)
    .expectHeaderContains('Content-Type', 'json')
.toss();

frisby.create('c_score is an empty string')
  .post(URL + '/judge/set', {
    j_name: 'DongWei',
		auth_code: authCode,
		r_id: 'NWQ01',
		c_id: 'NW001',
		c_score: ''
  }, {json: true})
  	.expectStatus(200)
    .expectHeaderContains('Content-Type', 'json')
.toss();

frisby.create('c_score is null')
  .post(URL + '/judge/set', {
    j_name: 'DongWei',
		auth_code: authCode,
		r_id: 'NWQ01',
		c_id: 'NW001',
		c_score: null
  }, {json: true})
  	.expectStatus(400)
    .expectHeaderContains('Content-Type', 'json')
.toss();

frisby.create('No r_id')
  .post(URL + '/judge/set', {
    j_name: 'DongWei',
		auth_code: authCode,
		c_id: 'NW001',
		c_score: '111BT'
  }, {json: true})
  	.expectStatus(400)
    .expectHeaderContains('Content-Type', 'json')
.toss();

frisby.create('Short r_id')
  .post(URL + '/judge/set', {
    j_name: 'DongWei',
		auth_code: authCode,
		r_id: 'NWQ1',
		c_id: 'NW001',
		c_score: '111BT'
  }, {json: true})
  	.expectStatus(400)
    .expectHeaderContains('Content-Type', 'json')
.toss();

frisby.create('Long r_id')
  .post(URL + '/judge/set', {
    j_name: 'DongWei',
		auth_code: authCode,
		r_id: 'NWQ001',
		c_id: 'NW001',
		c_score: '111BT'
  }, {json: true})
  	.expectStatus(400)
    .expectHeaderContains('Content-Type', 'json')
.toss();

frisby.create('r_id category does not exist, correct c_id')
  .post(URL + '/judge/set', {
    j_name: 'DongWei',
		auth_code: authCode,
		r_id: 'xxQ01',
		c_id: 'NW001',
		c_score: '111BT'
  }, {json: true})
  	.expectStatus(400)
    .expectHeaderContains('Content-Type', 'json')
.toss();

frisby.create('r_id category does not exist, wrong c_id')
  .post(URL + '/judge/set', {
    j_name: 'DongWei',
		auth_code: authCode,
		r_id: 'xxQ01',
		c_id: 'xx001',
		c_score: '111BT'
  }, {json: true})
  	.expectStatus(404)
    .expectHeaderContains('Content-Type', 'json')
.toss();

frisby.create('route does not exist, correct c_id')
  .post(URL + '/judge/set', {
    j_name: 'DongWei',
		auth_code: authCode,
		r_id: 'NWQ99',
		c_id: 'NW001',
		c_score: '111BT'
  }, {json: true})
  	.expectStatus(400)
    .expectHeaderContains('Content-Type', 'json')
.toss();

frisby.create('Short c_id')
  .post(URL + '/judge/set', {
    j_name: 'DongWei',
		auth_code: authCode,
		r_id: 'NMQ01',
		c_id: 'NM01',
		c_score: '111BT'
  }, {json: true})
  	.expectStatus(400)
    .expectHeaderContains('Content-Type', 'json')
.toss();

frisby.create('Long c_id')
  .post(URL + '/judge/set', {
    j_name: 'DongWei',
		auth_code: authCode,
		r_id: 'NMQ01',
		c_id: 'NM0001',
		c_score: '111BT'
  }, {json: true})
  	.expectStatus(400)
    .expectHeaderContains('Content-Type', 'json')
.toss();

frisby.create('Climber does not exist')
  .post(URL + '/judge/set', {
    j_name: 'DongWei',
		auth_code: authCode,
		r_id: 'NMQ01',
		c_id: 'NM999',
		c_score: '111BT'
  }, {json: true})
  	.expectStatus(404)
    .expectHeaderContains('Content-Type', 'json')
.toss();

frisby.create('Climber category does not exist')
  .post(URL + '/judge/set', {
    j_name: 'DongWei',
		auth_code: authCode,
		r_id: 'NMQ01',
		c_id: 'xx001',
		c_score: '111BT'
  }, {json: true})
  	.expectStatus(400)
    .expectHeaderContains('Content-Type', 'json')
.toss();


/*
frisby.create('')
  .get(URL + '/judge/get/')
	  .expectStatus(400)
	  .expectHeaderContains('Content-Type', 'application/json')
.toss();

frisby.create('')
  .post(URL + '/judge/set', {
    j_name: ,
		auth_code: ,
		r_id: ,
		c_id: ,
		c_score:
  }, {json: true})
  	.expectStatus(200)
    .expectHeaderContains('Content-Type', 'json')
.toss();
*/