#CRIMP-server

##GET ('/judge/get/:round')
:round [round expects 3 characters: NMQ/NWF]

<pre><code>
response (header: 200):
{
	"climbers": [
		{
			"c_id":"",
			"c_name":""
		}
	]
}
</code></pre>


##GET ('/judge/get/:c_id/:r_id')

:c_id [climber_id expect 5 characters: NW001/NM001]
* first 2 char to be category, last 3 char to be climber number

:r_id [route_id expect 5 characters: NWQ01/NMF01]
* first 3 char to be round name, last 2 char to be route number
* The first 2 character of c_id and r_id must match
* 3rd char of :r_id must be an alphabet

<pre><code>
response (header: 200):
{
	"c_name" : "climber_name",
	"c_score" : "magic_string"
}
</code></pre>



##POST ('/judge/set')

<pre><code>
body:
{
	"j_name" : "judge's name"
	"auth_code" : "",
	"r_id" : "route_id",
	"c_id" : "climber_id",
	"c_score" : "magic_string"
}

response (header: 200 / 401 Unauthorized):
{}
</code></pre>



##GET ('/admin/get/:r_id')

:r_id [route_id expect 5 characters: NWQ01/NMF01]
* first 3 char to be round name, last 2 char to be route number
* 3rd char of :r_id must be an alphabet

<pre><code>
response (header: 200):
{
	"climbers": {
		"c_id":
		"j_name":
		"c_name":
		"c_score":
	}
}
</code></pre>



##GET ('/admin/open/:auth_code')
Attempts to reopen websocket connection to CRIMP-socket

Look at logs for confirmation of successful connection

<pre><code>
response (header: 200 or 400)
</code></pre>



#CRIMP-socket
