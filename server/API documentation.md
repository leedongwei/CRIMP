#For judge

	##GET ('/judges/get/:c_id/:r_id')

	c:id [climber_id expect 5 characters: NW001/NM001]
		first 2 char to be category
		last 3 char to be climber number
	r:id [route_id expect 5 characters: NWQ01/NMF01]
		first 3 char to be round name
		last 2 char to be route number

		response:
		{
			"status" : "200"(ok) or "400"(bad syntax) or "404"(not found),
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

		response:
		{
			"status" : "200" or "400"(bad syntax) or "401"(unauthorized)
		}

#For client
In development