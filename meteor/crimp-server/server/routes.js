Restivus.configure({
  defaultHeaders: { 'Content-Type': 'application/json' },
  useAuth: true,
  auth: {
    'token': 'services.resume.loginTokens.hashedToken',
    'user': function() {
      return {
        userId: this.request.headers['x-user-id'],
        token: Accounts._hashLoginToken(this.request.headers['x-auth-token'])
      };
    }
  },
  prettyJson: true
});


Restivus.addRoute('judge/login', { authRequired: false }, {
  post: function () {
    if (!('accessToken' in this.bodyParams) ||
        !('expiresAt' in this.bodyParams)) {
      return { 'error': 'Missing fields' };
    }

    var fb, user;

    try {
      // TODO: HTTP request will block. Find a better solution.
      fb = HTTP.call('GET',
            'https://graph.facebook.com/v2.3/me?access_token=' + this.bodyParams.accessToken,
            { 'timeout': 333 });

      // Append data not returned by API call
      fb.data.accessToken = this.bodyParams.accessToken;
      fb.data.expiresAt = this.bodyParams.expiresAt;

      // Create user account
      user = Accounts.updateOrCreateUserFromExternalService(
        'facebook', fb.data, {'profile': {'name': fb.data.name }}
      );

      // Prevent multiple loginTokens on 1 account, doesn't seem to be needed
      // Accounts._clearAllLoginTokens(user.userId)

      // Create a loginToken and tie it to the account
      user.token = Accounts._generateStampedLoginToken();
      Accounts._insertLoginToken(user.userId, user.token);

      // Retrieve the role
      user.roles = Roles.getRolesForUser(user.userId);

      return {
        'x-user-id': user.userId,
        'x-auth-token': user.token.token,
        'roles': user.roles
      };
    } catch (e) {
      return { 'error': e.message };
    }
  }
});


Restivus.addRoute('judge/report',
      { authRequired: true, roleRequired: CRIMP.roles.organizers }, {
  post: function () {
    return {};
  }
});


Restivus.addRoute('judge/helpme',
      { authRequired: true, roleRequired: CRIMP.roles.organizers }, {
  post: function () {
    return { 'error': 'Endpoint is not implemented' };
  }
});


Restivus.addRoute('judge/categories',
      { authRequired: true, roleRequired: CRIMP.roles.organizers }, {
  get: function () {
    return Categories.find({}).fetch();
  }
});


Restivus.addRoute('judge/climbers/:category_id',
      { authRequired: true, roleRequired: CRIMP.roles.organizers }, {
  get: function () {
    var category = this.urlParams.category_id;

    // TODO: More checks needed?
    if (category.length !== 3) {
      return { 'error': 'Bad syntax for :category_id' };
    }

    // TODO: Clean up data, scores._id is being sent over
    return Climbers.find({ category_id: category }).fetch();
  }
});


Restivus.addRoute('judge/score/:route_id/:climber_id',
      { authRequired: true, roleRequired: CRIMP.roles.organizers }, {
  get: function() {
    var route = this.urlParams.route_id,
        climber = this.urlParams.climber_id;

    // TODO: More checks needed?
    if (route.substring(0, 3) !== climber.substring(0, 3)) {
      return { 'error': 'Route/Climber is from the wrong category' };
    }

    return Scores.findOne({
      climber_id: climber,
      route_id: route
    });
  },
  post: function() {
    var score = this.bodyParams.score,
        category = this.urlParams.route_id.substring(0, 3),
        selector = {
          'route_id': this.urlParams.route_id,
          'climber_id': this.urlParams.climber_id
        };

    var isFinalized = Categories.findOne({
      'category_id': category
    }).scores_finalized;

    if (isFinalized) {
      return { 'error': 'Scores for ' + category + ' has been finalized' }
    }

    // Retrieve previous score strings
    score = Scores.findOne(selector).score_string + score;

    var modifier = {
      'score_string': score,
      'score_top': CRIMP.scoring.calculateTop(score),
      'score_bonus': CRIMP.scoring.calculateBonus(score),
      'admin_id': this.userId
    };

    if (Scores.update(selector, {$set: modifier})) {
      return {};
    } else {
      return { 'error': 'Updated 0 scores' };
    }
  }
});