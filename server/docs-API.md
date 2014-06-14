#For judge

##GET ('/judges/get/:round')
	:round [:round expects 3 characters: NMQ/NWF]

	response:
	{
		"climbers": [
			{
				"c_id":"",
				"c_name":""
			}
		]
	}

##GET ('/judges/get/:c_id/:r_id')

	:c_id [climber_id expect 5 characters: NW001/NM001]
		first 2 char to be category
		last 3 char to be climber number
	:r_id [route_id expect 5 characters: NWQ01/NMF01]
		first 3 char to be round name
		last 2 char to be route number

	response:
	{
		"http_code" : "200"(ok) or "400"(bad syntax) or "404"(not found),
		"c_name" : "climber_name",
		"c_score" : "magic_string"
	}



##POST ('/judges/set')
	body:
	{
		"j_name" : "judge's name"
		"auth_code" : "",
		"r_id" : "route_id",
		"c_id" : "climber_id",
		"c_score" : "magic_string"
	}

	// it will INSERT to db, then SELECT and send the data from SELECT back to confirm that it is updated correctly
	response:
	{
		"http_code" : "200" or "400" or "401"(unauthorized) or "404"
		"r_id" : "route_id",
		"c_id" : "climber_id",
		"c_score" : "magic_string"
	}



#For client

##GET ('/client/get/:round')
	:round [round expects 3 characters: NWQ NMF]

	response:
	{
		"http_code":"200" or "404"
		"climbers": [
			{
				"c_name":
				"c_id":
				"tops":
				"t_attempts":
				"bonus":
				"b_attempts":
			}
		]
	}