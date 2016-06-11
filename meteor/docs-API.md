# CRIMP-server
* Communicates with spectator and admin web interface using DDP
* Exposes a HTTP, REST API for the judges' mobile app
* Endpoints expects `x-www-form-urlencoded`, or force all values to `String`
* When there is an error processing the request, the server will reply with the appropriate HTTP response code (i.e. 4xx, 5xx).
<br><br>


## API List
* [POST '/api/judge/login'](https://github.com/leedongwei/CRIMP/blob/develop/meteor/docs-API.md#post-apijudgelogin)
* [POST '/api/judge/logout'](https://github.com/leedongwei/CRIMP/blob/develop/meteor/docs-API.md#post-apijudgelogout)
* [GET '/api/judge/categories'](https://github.com/leedongwei/CRIMP/blob/develop/meteor/docs-API.md#get-apijudgecategories)
* [GET '/api/judge/score/{?climber_id}{?category_id}{?route_id}{?marker_id}'](https://github.com/leedongwei/CRIMP/blob/develop/meteor/docs-API.md#get-apijudgescoreclimber_idcategory_idroute_idmarker_id)
* [POST '/api/judge/score/:route_id/:climber_id'](https://github.com/leedongwei/CRIMP/blob/develop/meteor/docs-API.md#post-apijudgescoreroute_idmarker_id)
* [POST '/api/judge/helpme'](https://github.com/leedongwei/CRIMP/blob/develop/meteor/docs-API.md#post-apijudgehelpme)
* [POST '/api/judge/report'](https://github.com/leedongwei/CRIMP/blob/develop/meteor/docs-API.md#post-apijudgereport)
* [PUT '/api/judge/setactive'](https://github.com/leedongwei/CRIMP/blob/develop/meteor/docs-API.md#put-apijudgesetactive)
* [PUT '/api/judge/clearactive'](https://github.com/leedongwei/CRIMP/blob/develop/meteor/docs-API.md#put-apijudgeclearactive)
<br><br><br>



## POST '/api/judge/login'
* Login and authenticate user on the server.
* Creates a user account, or updates existing account
* Issues a authentication token for subsequent requests

#### Request
```json
Body: {
  "fb_access_token": "CAAE1913yZC2ABAAO6...",
}
```

#### Response
```json
Body: {
  "X-User-Id": "jfJnk4B...",
  "X-Auth-Token": "LNZoISu...",
  "remind_logout": true,
  "roles": ["admin"],
}
```
* `isProductionApp` prevents the situation of a judge using an old dev app
* `X-User-Id` and `X-Auth-Token` is used in endpoints requiring authorization
* `remind_logout` is `true` if there are existing sessions on other devices. The mobile app should display a reminder for user to log out on other devices.
* `roles` is the privilege level of the user
  * The mobile app should deny access to user if role is not higher than `judge`
  * Tokens will not be issued for users with insufficient role permissions
  * roles in increasing order of access: `denied`, `pending`, `partner`, `judge`, `admin`, `hukkataival`
    * `denied` is a stranger and is denied access
    * `pending` is a new user, and should be sorted by an admin
    * `partner` has read-only access through REST API
    * `judge` has read-write access through REST API
    * `admin` has read-write access through web dashboard
    * `hukkataival` is given all privileges
<br><br><br>



## POST '/api/judge/logout'
* Used to logout.

#### Request
```json
header: {
  "X-User-Id": "jfJnk4B...",
  "X-Auth-Token": "LNZoISu...",
}
```

#### Response
```json
body: {}
```
<br><br><br>


## GET '/api/judge/categories'
* Get information of all categories and routes.

#### Response
```json
Body: {
  "categories": [
    {
      "category_id": "e4gMzdjR...",
      "category_name": "Novice Men Qualifiers",
      "acronym": "NMQ",
      "is_score_finalized": false,
      "time_start": "Thu Jul 30 2015 12:00:00 GMT+0800",
      "time_end": "Thu Jul 30 2015 12:00:00 GMT+0800",
      "routes": [
        {
          "_id": "rbjJ...",
          "route_name": "Route 1",
          "score_rules": "points__1000"
        },
        {
          "_id": "TC3R...",
          "route_name": "Route 2",
          "score_rules": "points__800"
        },
        {
          "_id": "EgN4g...",
          "route_name": "Route 3",
          "score_rules": "points__1800"
        }
      ],
    },
    {
      "category_id": "e4gMzdjR...",
      "category_name": "Novice Women Qualifiers",
      "acronym": "NWQ",
      "is_team_category": true,
      "is_score_finalized": true,
      "time_start": "Thu Jul 30 2015 12:00:00 GMT+0800",
      "time_end": "Thu Jul 30 2015 12:00:00 GMT+0800",
      "routes": [
        {
          "_id": "rbjJ...",
          "route_name": "Route 1",
          "score_rules": "ifsc-top-bonus",
        },
        {
          "_id": "TC3R...",
          "route_name": "Route 2",
          "score_rules": "ifsc-top-bonus",
        },
        {
          "_id": "EgN4g...",
          "route_name": "Route 3",
          "score_rules": "ifsc-top-bonus",
        }
      ],
    },

    ...

  ]
}
```
* `category_id` uniquely identify a Category globally. Cannot be negative.
* `acronym` consist of three alphabet in uppercase and is unique.
* `route_id` uniquely identify a Route globally (not just within a Category). Cannot be negative.
* `score_type` inform the client which scoring system this route is supposed to use.
  * Accepted values: `IFSC Top-Bonus`, `TFBb`, `Points`
  * See `meteor/crimp-server/imports/score_systems/` folder for more documentation
]
<br><br><br>



## GET '/api/judge/score{?climber_id}{?category_id}{?route_id}{?marker_id}'
* Get score

#### Request
##### Query parameters
* `event_id` get scores from a specific event
* `category_id` get scores from a specific category
* `route_id` get scores from a specfic route
* `climber_id` get all the scores of a specific climber
* `marker_id` get scores of climbers with that marker_id

```json
header: {
  "X-User-Id": "jfJnk4B...",
  "X-Auth-Token": "LNZoISu...",
}
```

#### Response
##### No query parameters
```json
Body: {
  "climber_scores": [
    {
      "climber_id": "nJXAk...",
      "climber_name": "Antonio Paul",
      "scores": [
        {
          "marker_id": "NMQ004",
          "category_id": "n5jkl...",
          "route_id": "yGXAk...",
          "score": "1800"
        },
        {
          "marker_id": "NMQ004",
          "category_id": "n5jkl...",
          "route_id": "WryAk...",
          "score": "800"
        },
        ...
      ]
    },
    {
    "climber_id":  "io9aAk...",
    "climber_name": "Romani",
    "scores": [
      {
        "marker_id": "OMQ007",
        "category_id": "Q0afR...",
        "route_id": "FTHew...",
        "score": ""
      },
      {
        "marker_id": "OMQ007",
        "category_id": "Q0afR...",
        "route_id": "AX5Y4...",
        "score": "11BB1"
      }
    ]
    }
  ]
}
```

##### Query parameter: category_id=dBrvuk
```json
Body: {
  "climber_scores": [
    {
    "climber_id": "nJXAk...",
    "climber_name": "Antonio Paul",
    "scores": [
      {
        "marker_id": "NMQ004",
        "category_id": "dBrvuk",
        "route_id": "yGXAk...",
        "score": "1800"
      },
      {
        "marker_id": "NMQ004",
        "category_id": "dBrvuk",
        "route_id": "t0aTUD...",
        "score": "800"
      },
    ...
    ]
  },
  {
    "climber_id": "io9aAk...",
    "climber_name": "Romani",
    "scores": [
      {
      "marker_id": "NMQ008",
      "category_id": "dBrvuk",
      "route_id": "yGXAk...",
      "score": "0"
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
      "climber_id": "nJXAk...",
      "climber_name": "Antonio Paul",
      "scores": [
        {
          "marker_id": "NMQ004",
          "category_id": "dBrvuk",
          "route_id": "t0aTUD...",
          "score": "800"
        },
      ]
    }
  ]
}
```
* Response is always as much information as possible that satisfy the query parameter (e.g. if climber is in multiple route/category and the query parameter only restrict the climber_id, all routes score will be returned by server).
* If a climber is in more than one route, all route score for that climber will be returned even if the climber did not attempt some of the route (i.e. ""score": "").
<br><br><br>



## POST '/api/judge/score/:route_id/:marker_id'
* Used by judges to update the score of a climber on a specific route
* If `scores_finalized` is `true` for a category, then the scores will not be updated any more.
  * You'll receive error response code.

#### Request
```json
header: {
  "X-User-Id": "jfJnk4B...",
  "X-Auth-Token": "LNZoISu...",
}
body: {
  "score_string": "11T"
}
```

#### Response
```json
body: {
  "climber_id": "climberId1",
  "category_id": "e4gMzdjR...",
  "route_id": "rbjJ...",
  "marker_id": "NMF002",
  "score": "11B11T"
}
```
* Response is the current state as seen by server.
* `score_string` should only cover climb on that attempt and are appended to whatever score_string accumulated by previous attempt.
  * e.g. sending in `11B` followed by `1T` will make the overall score_string to be `11B1T`.
* `score_string` is a raw value with no semantics. Interpretation of this field should be done by the client.
<br><br><br>



## POST '/api/judge/helpme'
* Used by judges to request help from the admin.

#### Request
```json
header: {
  "X-User-Id": "jfJnk4B...",
  "X-Auth-Token": "LNZoISu...",
}
body: {
  "route_id": "EgN4g...",
}
```

#### Response
```json
body: {}
```
* Response is immediate to acknowledge that the server has received it. It does not mean that the admin has acknowledged it.
<br><br><br>



## POST '/api/judge/report'
* Used to inform CRIMP server that this user will attempt to judge a route. Provides a way to resolve conflict when there are multiple user trying to judge the same route.
* Used by admin to make sure that the judges are on the correct route.

#### Request
```json
header: {
  "X-User-Id": "jfJnk4B...",
  "X-Auth-Token": "LNZoISu...",
}

body: {
  "category_id": "e4gMzdjR...",
  "route_id": "EgN4g...",
  "force": false
}
```

#### Response
```json
Body: {
  "X-User-Id": "jfJnk4B...",
  "user_name": "Weizhi",
  "category_id": "e4gMzdjR...",
  "route_id": "EgN4g...",
}
```
* `fb_user_id` and `user_name` refers to the active judge as seen by server.
<br><br><br>



## PUT '/api/judge/setactive'
* Set a climber for a route

#### Request
```json
header: {
  "X-User-Id": "jfJnk4B...",
  "X-Auth-Token": "LNZoISu...",
}
body: {
  "route_id": "yGXAk...",
  "marker_id": "NMF001",
}
```

#### Response
```json
Body: {}
```
<br><br><br>



## PUT '/api/judge/clearactive'
* clear a climber for a route

#### Request
```json
header: {
  "X-User-Id": "jfJnk4B...",
  "X-Auth-Token": "LNZoISu...",
}
body: {
  "route_id": "yGXAk...",
}
```

#### Response
```json
Body: {}
```
<br><br><br>


