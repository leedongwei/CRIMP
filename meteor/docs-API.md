# CRIMP-server
* Communicates with spectator and admin web interface using DDP
* Exposes a HTTP, REST API for the judges' mobile app
* When there is an error processing the request, the server will reply with the appropriate HTTP response code (i.e. 4xx, 5xx).

<br><br><br>



## API List
* [GET '/api/judge/categories'](https://github.com/leedongwei/CRIMP/blob/develop/meteor/docs-API.md#get-apijudgecategories)
* [GET '/api/judge/climberscore/{?climber_id}{?category_id}{?route_id}{?marker_id}'](https://github.com/leedongwei/CRIMP/blob/develop/meteor/docs-API.md#get-apijudgeclimberscoreclimber_idcategory_idroute_idmarker_id)
* [PUT '/api/judge/setactive'](https://github.com/leedongwei/CRIMP/blob/develop/meteor/docs-API.md#put-apijudgesetactive)
* [PUT '/api/judge/clearactive'](https://github.com/leedongwei/CRIMP/blob/develop/meteor/docs-API.md#put-apijudgeclearactive)
* [POST '/api/judge/login'](https://github.com/leedongwei/CRIMP/blob/develop/meteor/docs-API.md#post-apijudgelogin)
* [POST '/api/judge/report'](https://github.com/leedongwei/CRIMP/blob/develop/meteor/docs-API.md#post-apijudgereport)
* [POST '/api/judge/helpme'](https://github.com/leedongwei/CRIMP/blob/develop/meteor/docs-API.md#post-apijudgehelpme)
* [POST '/api/judge/score/:route_id/:climber_id'](https://github.com/leedongwei/CRIMP/blob/develop/meteor/docs-API.md#post-apijudgescoreroute_idclimber_id)
* [POST '/api/judge/logout'](https://github.com/leedongwei/CRIMP/blob/develop/meteor/docs-API.md#post-apijudgelogout)
<br><br><br>



## GET '/api/judge/categories'
* Get information of all categories and routes.

#### Response
```json
Body: {
  "categories": [
    {
	  "category_id": 23,
      "category_name": "Novice Men Qualifiers",
      "acronym": "NMQ",
      "routes": [
        {
          "route_id": 67,
          "route_name": "Route 1",
          "score_type": "1200",
		  "score_finalized": false,
		  "time_start": "Thu Jul 30 2015 12:00:00 GMT+0800",
		  "time_end": "Thu Jul 30 2015 12:00:00 GMT+0800"
        },
        {
          "route_id": 68,
          "route_name": "Route 2",
          "score_type": "1000",
		  "score_finalized": false,
		  "time_start": "Thu Jul 30 2015 12:00:00 GMT+0800",
		  "time_end": "Thu Jul 30 2015 12:00:00 GMT+0800"
        },
        {
          "route_id": 69,
          "route_name": "Route 3",
          "score_type": "1800",
		  "score_finalized": false,
		  "time_start": "Thu Jul 30 2015 12:00:00 GMT+0800",
		  "time_end": "Thu Jul 30 2015 12:00:00 GMT+0800"
        }
      ]
    },
    {
      "category_id": 24,
      "category_name": "Novice Women Qualifiers",
      "acronym": "NWQ",
      "routes": [
        {
          "route_id": 70,
          "route_name": "Route 1",
          "score_type": "TB",
		  "score_finalized": false,
		  "time_start": "Thu Jul 30 2015 12:00:00 GMT+0800",
		  "time_end": "Thu Jul 30 2015 12:00:00 GMT+0800"
        },
        {
          "route_id": 71,
          "route_name": "Route 2",
          "score_type": "TB",
		  "score_finalized": false,
		  "time_start": "Thu Jul 30 2015 12:00:00 GMT+0800",
		  "time_end": "Thu Jul 30 2015 12:00:00 GMT+0800"
        },
        {
          "route_id": 72,
          "route_name": "Route 3",
          "score_type": "TB",
		  "score_finalized": false,
		  "time_start": "Thu Jul 30 2015 12:00:00 GMT+0800",
		  "time_end": "Thu Jul 30 2015 12:00:00 GMT+0800"
        }
      ]
    },

    ...

  ]
}
```
* `category_id` uniquely identify a Category globally. Cannot be negative.
* `acronym` consist of three alphabet in uppercase and is unique.
* `route_id` uniquely identify a Route globally (not just within a Category). Cannot be negative.
* `score_type` inform the client which scoring system this route is supposed to use.
<br><br><br>



## GET '/api/judge/climberscore/{?climber_id}{?category_id}{?route_id}{?marker_id}'
* Get score

#### Request
##### Query parameters
* `climber_id` get score with this climber_id.
* `category_id` get score with this category_id.
* `route_id` get score with this route_id.
* `marker_id` get score with this marker_id.

```json
header: {
  "fb_user_id": 29,
  "fb_access_token": "RCDBy6X3zS8...",
  "sequential_token": 98
}
```

#### Response
##### No query parameters
```json
Body: {
  "climber_scores": [
    {
	  "climber_id": 14,
	  "climber_name": "Antonio Paul",
	  "scores": [
	    {
		  "marker_id": "NMQ004",
		  "category_id": 26,
		  "route_id": 53,
		  "score": "11B1T"
		},
		{
		  "marker_id": "NMQ004",
		  "category_id": 26,
		  "route_id": 54,
		  "score": "11"
		},
		{
		  "marker_id": "NMQ004",
		  "category_id": 26,
		  "route_id": 55,
		  "score": "11T"
		},
		{
		  "marker_id": "IMQ024",
		  "category_id": 59,
		  "route_id": 234,
		  "score": "2360"
		}
	  ]
	},
	{
	  "climber_id": 15,
	  "climber_name": "Romani",
	  "scores": [
	    {
		  "marker_id": "OMQ007",
		  "category_id": 2,
		  "route_id": 47,
		  "score": ""
		},
		{
		  "marker_id": "OMQ007",
		  "category_id": 2,
		  "route_id": 48,
		  "score": "11BB"
		}
	  ]
	}
  ]
}
```

##### Query parameter: category_id=26
```json
Body: {
  "climber_scores": [
    {
	  "climber_id": 14,
	  "climber_name": "Antonio Paul",
	  "scores": [
	    {
		  "marker_id": "NMQ004",
		  "category_id": 26,
		  "route_id": 53,
		  "score": "11B1T"
		},
		{
		  "marker_id": "NMQ004",
		  "category_id": 26,
		  "route_id": 54,
		  "score": "11"
		},
		{
		  "marker_id": "NMQ004",
		  "category_id": 26,
		  "route_id": 55,
		  "score": "11T"
		}
	  ]
	},
	{
	  "climber_id": 15,
	  "climber_name": "Romani",
	  "scores": [
	    {
		  "marker_id": "NMQ008",
		  "category_id": 26,
		  "route_id": 47,
		  "score": ""
		}
	  ]
	}
  ]
}
```

##### Query parameter: climber_id=14&route_id=54
```json
Body: {
  "climber_scores": [
    {
	  "climber_id": 14,
	  "climber_name": "Antonio Paul",
	  "scores": [
		{
		  "marker_id": "NMQ004",
		  "category_id": 26,
		  "route_id": 54,
		  "score": "11"
		}
	  ]
	}
  ]
}
```
* Response is always as much information as possible that satisfy the query parameter (e.g. if climber is in multiple route/category and the query parameter only restrict the climber_id, all routes score will be returned by server).
* If a climber is in more than one route, all route score for that climber will be returned even if the climber did not attempt some of the route (i.e. ""score": "").
<br><br><br>



## PUT '/api/judge/setactive'
* Set a climber for a route

#### Request
```json
header: {
  "fb_user_id": 28,
  "fb_access_token": "RCDBy6X3zS8...",
  "sequential_token": 98
}
body: {
  "route_id": 14,
  "marker_id": "NMF001"            
}
```

#### Response
```json
Body: {
  "climber_id": 79,
  "climber_name": "Dongie",
  "active_route": 14
}
```
<br><br><br>



## PUT '/api/judge/clearactive'
* clear a climber for a route

#### Request
```json
header: {
  "fb_user_id": 28,
  "fb_access_token": "RCDBy6X3zS8...",
  "sequential_token": 98
}
body: {
  "route_id": 14  
}
```

#### Response
```json
Body: {
  "route_id": 14
}
```
<br><br><br>



## POST '/api/judge/login'
* Login and let the server know about user.

#### Request
```json
Body: {
  "fb_user_id": 23,
  "fb_access_token": "CAAE1913yZC2ABAAO6...",
  "force_login": true
}
```

#### Response
```json
Body: {
  "fb_user_id": 23,
  "fb_access_token": "CAAE1913yZC2ABAAO6...",
  "user_name": "John Doe",
  "remind_logout": true,
  "sequential_token": 1
}
```
* `sequential_token` cannot be negative.
<br><br><br>



## POST '/api/judge/report'
* Used to inform CRIMP server that this user will attempt to judge a route. Provides a way to resolve conflict when there are multiple user trying to judge the same route.
* Used by admin to make sure that the judges are on the correct route.

#### Request
```json
header: {
  "fb_user_id": 23,
  "fb_access_token": "RCDBy6X3zS8...",
  "sequential_token": 98
}

body: {
  "category_id": 14,
  "route_id": 66,
  "force": false
}
```

#### Response
```json
Body: {
  "fb_user_id": 23,
  "user_name": "Weizhi",
  "category_id": "14",
  "route_id": "66"
}
```
* `fb_user_id` and `user_name` refers to the active judge as seen by server.
<br><br><br>



## POST '/api/judge/helpme'
* Used by judges to request help from the admin.

#### Request
```json
header: {
  "fb_user_id": 28,
  "fb_access_token": "RCDBy6X3zS8...",
  "sequential_token": 98
}
body: {
  "route_id": 33
}
```

#### Response
```json
body: {
  "fb_user_id": 28,
  "fb_access_token": "RCDBy6X3zS8...",
  "route_id": 33
}
```
* Response is immediate to acknowledge that the server has received it. It does not mean that the admin has acknowledged it.
<br><br><br>



## POST '/api/judge/score/:route_id/:climber_id'
* Used by judges to update the score of a climber on a specific route
* If `scores_finalized` is `true` for a category, then the scores will not be updated any more.
  * You'll receive error response code.

#### Request
```json
header: {
  "fb_user_id": 28,
  "fb_access_token": "RCDBy6X3zS8...",
  "sequential_token": 98
}
body: {
  "score_string": "11T"
}
```

#### Response
```json
body: {
  "climber_id": 14,
  "climber_name": "Antonio Paul",
  "category_id": 26,
  "route_id": 53,
  "score": "11B11T"
}
```
* Response is the current state as seen by server.
* `score_string` should only cover climb on that attempt and are appended to whatever score_string accumulated by previous attempt.
  * e.g. sending in `11B` followed by `1T` will make the overall score_string to be `11B1T`.
* `score_string` is a raw value with no semantics. Interpretation of this field should be done by the client.
<br><br><br>



## POST '/api/judge/logout'
* Used to logout.

#### Request
```json
body: {
  "fb_user_id": 28,
  "fb_access_token": "RCDBy6X3zS8...",
  "sequential_token": 98
}
```

#### Response
```json
body: {}
```
