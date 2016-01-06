# CRIMP-server
* Communicates with spectator and admin web interface using DDP
* Exposes a HTTP, REST API for the judges' mobile app
  * When there is an error processing the request, the response body will have the key `error` with the explanation

<br>


## POST '/api/judge/login'
* Used by judges to login or create an account
* Authenticate with Facebook only

#### Request
```
Body: {
  accessToken: 'CAAE1913yZC2ABAAO6...',
  isProductionApp: true     // ignored on staging
}
```

#### Response
```
Body: {
  x-user-id: 'A6kvTowyvNz...',
  x-auth-token: 'RCDBy6X3zS8...'
}
```
* `roles` in increasing order of access: denied, pending, partner, judge, admin, hukkataival
* Store `x-user-id` and `x-auth-token` for subsequent requests
* To generate a new token, log out and in again
* `GET /api/logout` to destroy the session


<br><br><br>


## POST '/api/judge/report'
* Used by judges to report in when they are judging a route, and to inform everyone if there is a clash (e.g. 2 judges trying to report in on the same route)
* Used by admin to make sure that the judges are on the right route

#### Request
```
header: {
  x-user-id: 'A6kvTowyvNz...',
  x-auth-token: 'RCDBy6X3zS8...'
}

body: {
  category_id: 'NWQ',
  route_id: 'R1',
  force: false
}
```
* Server will track the judges currently active (on scoring duty)
* If there is no activity from a judge for 10mins, he will be dropped
* Removal can be enforced by setting `force` to true, which covers the case when judges are substituted from duty

#### Response
```
Body: {
  admin_id: 'A6kvTowyvNz...',
  admin_name: 'Weizhi'
  category_id: 'NWQ'
  route_id: 'R1'
  state: 1
}
```
* `admin_id` and `admin_name` refers to the active judge on that route
* `state` 1 if you're successfully set as judge, 0 if you're not the judge
* `admin_id` and `admin_name` be your's if successful, someone else's if failed.
* Request will fail when there is already a judge on that route


<br><br><br>


## POST '/api/judge/helpme'
* Used by judges to summon help from the admin to their station
* <b>Not implemented yet</b>

#### Request
```
header: {
  x-user-id: 'A6kvTowyvNz...',
  x-auth-token: 'RCDBy6X3zS8...'
}
body: {
  category_id: 'NWQ'
  route_id: 'NWQ1'
}
```

#### Response
```
body: {}
```
* Response is immediate to acknowledge that the server has received it. It does not mean that the admin has acknowledged it. Check for `error` to see if it failed.


<br><br><br>


## GET '/api/judge/categories'
* Used by judges to get data on all the categories

#### Request
```
header: {
  x-user-id: 'A6kvTowyvNz...',
  x-auth-token: 'RCDBy6X3zS8...'
}
```

#### Response
```
Body: {
  categories: [
    {
      category_name: 'Novice Men Qualifiers',
      category_id: 'NMQ',
      routes: [
        {
          route_id: 'R1',
          route_name: 'Route 1',
          score: '1200'
        },
        {
          route_id: 'R2',
          route_name: 'Route 2',
          score: '2300'
        },
        {
          route_id: 'R3',
          route_name: 'Route 3'
          score: '1254'
        }
      ],
      scores_finalized: false,
      time_start: Thu Jul 30 2015 12:00:00 GMT+0800,
      time_end: Thu Jul 30 2015 18:00:00 GMT+0800
    },
    {
      category_name: 'Novice Woman Qualifiers',
      category_id: 'NWQ',
      routes: [
        {
          route_id: 'R1',
          route_name: 'Route 1',
          score: '1200'
        },
        {
          route_id: 'R2',
          route_name: 'Route 2',
          score: '2300'
        },
        {
          route_id: 'R3',
          route_name: 'Route 3'
          score: '1254'
        }
      ],
      scores_finalized: false,
      time_start: Thu Jul 30 2015 12:00:00 GMT+0800,
      time_end: Thu Jul 30 2015 18:00:00 GMT+0800
    },

    ...

  ]
}
````
* The convention for `route_id` is [3-char category_id][number]
  * The routes for NMQ are NMQ1, NMQ2 ... NMQ6.
  * For categories with 10 routes, it would be NMQ10, NMQ100, etc
* The convention for `climber_id` is [3-char category_id][3-char number]
  * The climbers for NMQ are NMQ001, NMQ002 ... NMQ010 ... NMQ999.
  * If you're running an event with >999 climbers in 1 category, congrats and allez.


<br><br><br>


## GET '/api/judge/climber/:climber_id'
* Used by judges to get identity of all the climbers in a specific category

#### Request
```
header: {
  x-user-id: 'A6kvTowyvNz...',
  x-auth-token: 'RCDBy6X3zS8...'
}
```

#### Response
```
Body: {
  climber_id: 'NMQ001',
  climber_name: 'DongWei',
  total_score: '5T8 5B6'
}
````


<br><br><br>


## POST '/api/judge/activemonitor'
* Insert/remove a climber from ActiveMonitor

#### Request
```
header: {
  x-user-id: 'A6kvTowyvNz...',
  x-auth-token: 'RCDBy6X3zS8...'
}
body: {
  category_id: 'NMQ',
  route_id: 'NMQ1',
  climber_id: 'NMQ009',     // not needed for removal
  insert: true              // true => insert, false => remove
}
```

#### Response
```
Body: {}
```
* Check response for `error` to see if it failed.


<br><br><br>


## GET '/api/judge/score/:category_id/:route_id/:climber_id'
* Used by judges to get the score of a climber on a specific route

#### Request
```
header: {
  x-user-id: 'A6kvTowyvNz...',
  x-auth-token: 'RCDBy6X3zS8...'
}
```

#### Response
```
Body: {
  category_id: 'NMQ',
  route_id: 'NMQ6'
  climber_id: 'NMQ001',
  climber_name: 'DongWei',
  score_string: '11B11'
}
```


<br><br><br>


## POST '/api/judge/score/:category_id/:route_id/:climber_id'
* Used by judges to update the score of a climber on a specific route
* If `scores_finalized` is `true` for a category, then the scores will not be updated any more.
  * You'll receive `error` in response
* A successful score update will remove climber from `ActiveClimber`

#### Request
```
header: {
  x-user-id: 'A6kvTowyvNz...',
  x-auth-token: 'RCDBy6X3zS8...'
}
body: {
  score_string: '11T'    // Important! See note below.
}
```

#### Response
```
body: {}
```
* Check response for `error` to see if it failed.
* Note: `score` should only cover climbs on that attempt
  * e.g. sending in '11T' for DongWei makes his overall score for this route to be '11B1111T'
