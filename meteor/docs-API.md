# CRIMP-server
* Communicates with spectator and admin web interface using DDP
* Exposes a HTTPS only, REST API for the judges' mobile app
  * `Content-Type` is always `application/json`
  * When there is an error processing the request, the response body will have the key `error` with the explanation

<br>


## POST '/api/judge/login'
* Used by judges to login or create an account
* Authenticates with Facebook only

#### Request
```
Body: {
  accessToken: 'CAAE1913yZC2ABAAO6...'
  expiresAt: '143961...'
}
```

#### Response
```
Body: {
  x-user-id: 'A6kvTowyvNz...',
  x-auth-token: 'RCDBy6X3zS8...',
  roles: ['admin']
}
```
* `roles` in increasing order of access: denied, pending, partner, judge, admin, hukkataival
* Store `x-user-id` and `x-auth-token` for subsequent requests
* `x-auth-token` will be hashed. To generate a new token, log out
* `GET` 'api/logout' to destroy the session


<br><br><br>


## POST '/api/judge/report'
* Used by judges to report in when they are judging a route, and to inform everyone if there is a clash
* Used by admin to make sure that the judges are on the right route

#### Request
```
header: {
  x-user-id: 'A6kvTowyvNz...',
  x-auth-token: 'RCDBy6X3zS8...'
}
body: {
  category_id: 'NWQ'
  route_id: 'NWQ1
  force: false
}
```
* Server will track the judges currently active (on scoring duty)
* If there is no activity from a judge for 10mins, he will be removed from the array
* Removal can be enforced by setting `force` to true, which covers the case when judges are substituted from duty

#### Response
```
Body: {
  admin_id: 'A6kvTowyvNz...',
  admin_name: 'Weizhi'
  category_id: 'NWQ',
  route_id: 'NWQ01'
  state: 1
}
```
* `admin_id` and `admin_name` refers to the active judge on that route
* `state` 1 for successful request, 0 for failed request
* `admin_id` and `admin_name` be your's if successful, someone else's if failed.
* Request will fail when there is already a judge on that route


<br><br><br>


## POST 'judge/helpme'
* Used by judges to summon help from the admin to their station

#### Request
```
header: {
  x-user-id: 'A6kvTowyvNz...',
  x-auth-token: 'RCDBy6X3zS8...'
}
body: {
  admin_id: 'A6kvTowyvNz...',
  route_id: 'NWQ1'
}
```

#### Response
```
body: {
  status: 'received'
}
```


<br><br><br>


## GET 'judge/categories'
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
      category_name: 'NMQ001',
      category_id: 'DongWei',
      route_count: 6,
      scores_finalized: false,
      time_start: {format undecided},
      time_end: {format undecided},
    },
    {
      category_name: 'NMQ001',
      category_id: 'DongWei',
      route_count: 6,
      scores_finalized: false,
      time_start: {format undecided},
      time_end: {format undecided},
    },

    ...

  ]
}
````


<br><br><br>


## GET 'judge/climbers/:category_id'
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
Status: 200 OK
Body: {
  category_id: 'NWQ'
  climbers: [
    {
      climber_id: 'NMQ001'
      climber_name: 'DongWei'
    },
    {
      climber_id: 'NMQ002',
      climber_name: 'Weizhi'
    },

    ...

  ]
}
````


<br><br><br>


## GET 'judge/score/:route_id/:climber_id'
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
Status: 200 OK
Body: {
  route_id: 'NMQ001'
  climber_id: 'NMQ001',
  climber_name: 'DongWei',
  score: '11B11'
}
```


<br><br><br>


## POST 'judge/score/:route_id/:climber_id'
* Used by judges to update the score of a climber on a specific route
* If `scores_finalized` is `true` for a category, then the scores will not be updated any more.

#### Request
```
header: {
  x-user-id: 'A6kvTowyvNz...',
  x-auth-token: 'RCDBy6X3zS8...'
}
body: {
  score: '11T'
}
```

#### Response
```
Status: 200 OK / 401 Unauthorized
body: {
  // if there is an error
  error: 'Scores are finalized' / 'Unauthorized'
}
```

* `score` should only cover climbs on that attempt
  * e.g. sending in '11T' for DongWei makes his overall score to be '11B1111T'

