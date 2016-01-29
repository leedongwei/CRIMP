# CRIMP-server
* Communicates with spectator and admin web interface using DDP
* Exposes a HTTP, REST API for the judges' mobile app
* When there is an error processing the request, the server will reply with the appropriate HTTP response code (i.e. 4xx, 5xx).

<br><br><br>

## Terminology
* `category_id` is a unique identifier for category and consist of three alphanumeric characters in uppercase (e.g. `NMF`, `IA3`). A category can be associated with multiple routes.
* `route_id` is a unique identifier for a route. A route must be associated with a single category. `route_id` consist of six alphanumeric characters in uppercase. The first three characters in `route_id` must be the same as the `category_id` this route is associated with (e.g. `route_id: NMF002` is associated with `category_id: NMF`). No two routes can have the same `route_id` even if they are associated with different category.
* `climber_id` is a unique identifier for a climber within a category. A `climber_id` must be associated with a single category. `climber_id` consist of the `category_id` it is associated with followed by three digits (e.g. `climber_id: NMF014` is associated with `category_id: NMF`). A climber can have multiple `climber_id` each for a different category.
* `accessToken` is the access token issued by facebook upon login.
* `x-user-id` is an id given by CRIMP server.
* `x-auth-token` is an authentication token given by CRIMP server.
<br><br><br>



## API List
* [GET '/api/judge/categories'](https://github.com/leedongwei/CRIMP/blob/feature/dongwei/meteor/docs-API.md#get-apijudgecategories)
* [GET '/api/judge/climber/:climber_id'](https://github.com/leedongwei/CRIMP/blob/feature/dongwei/meteor/docs-API.md#get-apijudgeclimberclimber_id)
* [GET '/api/judge/score/:category_id/:route_id/:climber_id'](https://github.com/leedongwei/CRIMP/blob/feature/dongwei/meteor/docs-API.md#get-apijudgescorecategory_idroute_idclimber_id)
* [POST '/api/judge/login'](https://github.com/leedongwei/CRIMP/blob/feature/dongwei/meteor/docs-API.md#post-apijudgelogin)
* [POST '/api/judge/report'](https://github.com/leedongwei/CRIMP/blob/feature/dongwei/meteor/docs-API.md#post-apijudgereport)
* [POST '/api/judge/helpme'](https://github.com/leedongwei/CRIMP/blob/feature/dongwei/meteor/docs-API.md#post-apijudgehelpme)
* [POST '/api/judge/activemonitor'](https://github.com/leedongwei/CRIMP/blob/feature/dongwei/meteor/docs-API.md#post-apijudgeactivemonitor)
* [POST '/api/judge/score/:category_id/:route_id/:climber_id'](https://github.com/leedongwei/CRIMP/blob/feature/dongwei/meteor/docs-API.md#post-apijudgescorecategory_idroute_idclimber_id)
<br><br><br>



## GET '/api/judge/categories'
* Get information of all categories and routes.

#### Request
```json
header: {
  "x-user-id": "A6kvTowyvNz...",
  "x-auth-token": "RCDBy6X3zS8..."
}
```

#### Response
```json
Body: {
  "categories": [
    {
      "category_name": "Novice Men Qualifiers",
      "category_id": "NMQ",
      "routes": [
        {
          "route_id": "NMQRO1",
          "route_name": "Route 1",
          "score": "1200"
        },
        {
          "route_id": "NMQRO2",
          "route_name": "Route 2",
          "score": "2300"
        },
        {
          "route_id": "NMQRO3",
          "route_name": "Route 3",
          "score": "1254"
        }
      ],
      "scores_finalized": false,
      "time_start": "Thu Jul 30 2015 12:00:00 GMT+0800",
      "time_end": "Thu Jul 30 2015 18:00:00 GMT+0800"
    },
    {
      "category_name": "Novice Woman Qualifiers",
      "category_id": "NWQ",
      "routes": [
        {
          "route_id": "NWQRO1",
          "route_name": "Route 1",
          "score": "1200"
        },
        {
          "route_id": "NWQRO2",
          "route_name": "Route 2",
          "score": "2300"
        },
        {
          "route_id": "NWQRO3",
          "route_name": "Route 3",
          "score": "1254"
        }
      ],
      "scores_finalized": false,
      "time_start": "Thu Jul 30 2015 12:00:00 GMT+0800",
      "time_end: Thu Jul 30 2015 18:00:00 GMT+0800"
    },

    ...

  ]
}
```
* `score`, `scores_finalized`, `time_start` and `time_end` are for features that are yet to be implemented. Client should expect these fields to be present during API calls but should not use them for any meaningful operations.
<br><br><br>



## GET '/api/judge/climber/:climber_id'
* Get information of a climber within a category.
* Not used.

#### Request
```json
header: {
  "x-user-id": "A6kvTowyvNz...",
  "x-auth-token": "RCDBy6X3zS8..."
}
```

#### Response
```json
Body: {
  "climber_id": "NMQ001",
  "climber_name": "DongWei",
  "total_score": "5T8 5B6"
}
```
* `total_score` is for features that are yet to be implemented. Client should expect this field to be present during API calls but should not use is for any meaningful operations.
<br><br><br>



## GET '/api/judge/score/:category_id/:route_id/:climber_id'
* Get the score of a climber on a specific route

#### Request
```json
header: {
  "x-user-id": "A6kvTowyvNz...",
  "x-auth-token": "RCDBy6X3zS8..."
}
```

#### Response
```json
Body: {
  "category_id": "NMQ",
  "route_id": "NMQBB6",
  "climber_id": "NMQ001",
  "climber_name": "DongWei",
  "score_string": "11B11"
}
```
* `score_string` is a raw value with no semantics. Interpretation of this field should be done by the client.
<br><br><br>



## POST '/api/judge/login'
* Obtain credential necesary for making other API calls using a facebook access token.

#### Request
```json
Body: {
  "accessToken": "CAAE1913yZC2ABAAO6...",
  "isProductionApp": true
}
```
* `isProductionApp` is ignored on staging.

#### Response
```json
Body: {
  "x-user-id": "A6kvTowyvNz...",
  "x-auth-token": "RCDBy6X3zS8..."
}
```
<br><br><br>



## POST '/api/judge/report'
* Used to inform CRIMP server that this user will attempt to judge a route. Provides a way to resolve conflict when there are multiple user trying to judge the same route.
* Used by admin to make sure that the judges are on the right route

#### Request
```json
header: {
  "x-user-id": "A6kvTowyvNz...",
  "x-auth-token": "RCDBy6X3zS8..."
}

body: {
  "category_id": "NWQ",
  "route_id": "NWQRO1",
  "force": false
}
```
* Server will track the judges currently active (on scoring duty)
* If there is no activity from a judge for 10mins, he will be dropped
* Removal can be enforced by setting `force` to true, which covers the case when judges are substituted from duty

#### Response
```json
Body: {
  "admin_id": "A6kvTowyvNz...",
  "admin_name": "Weizhi",
  "category_id": "NWQ",
  "route_id": "NWQRO1",
  "state": 1
}
```
* `admin_id` and `admin_name` refers to the active judge on that route
* `state` 1 if you're successfully set as judge, 0 if you're not the judge
* `admin_id` and `admin_name` be your's if successful, someone else's if failed.
* Request will fail when there is already a judge on that route
<br><br><br>



## POST '/api/judge/helpme'
* Used by judges to request help from the admin.
* <b>Not implemented yet</b>

#### Request
```json
header: {
  "x-user-id": "A6kvTowyvNz...",
  "x-auth-token": "RCDBy6X3zS8..."
}
body: {
  "category_id": "NWQ",
  "route_id": "NWQRO1"
}
```

#### Response
```json
body: {}
```
* Response is immediate to acknowledge that the server has received it. It does not mean that the admin has acknowledged it.
<br><br><br>



## POST '/api/judge/activemonitor'
* Insert/remove a climber from ActiveMonitor

#### Request
```json
header: {
  "x-user-id": "A6kvTowyvNz...",
  "x-auth-token": "RCDBy6X3zS8..."
}
body: {
  "category_id": "NMQ",
  "route_id": "NMQRO1",
  "climber_id": "NMQ009",     
  "insert": true              
}
```
* `climber_id` is not needed for removal.
* `insert` if true then insert else remove.

#### Response
```json
Body: {}
```
<br><br><br>



## POST '/api/judge/score/:category_id/:route_id/:climber_id'
* Used by judges to update the score of a climber on a specific route
* If `scores_finalized` is `true` for a category, then the scores will not be updated any more.
  * You'll receive error response code.
* A successful score update will remove climber from `ActiveClimber`

#### Request
```json
header: {
  "x-user-id": "A6kvTowyvNz...",
  "x-auth-token": "RCDBy6X3zS8..."
}
body: {
  "score_string": "11T"    
}
```

#### Response
```json
body: {}
```
* Check response code to see if it failed.
* `score_string` should only cover climb on that attempt and are appended to whatever score_string accumulated by previous attempt.
  * e.g. sending in `11B` followed by `1T` will make the overall score_string to be `11B1T`.
* `score_string` is a raw value with no semantics. Interpretation of this field should be done by the client.
