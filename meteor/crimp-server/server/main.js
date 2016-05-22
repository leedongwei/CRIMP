import { Meteor } from 'meteor/meteor';
import { HTTP } from 'meteor/http';
import { Accounts } from 'meteor/accounts-base';
import { Roles } from 'meteor/alanning:roles';
import { Restivus } from 'meteor/nimble:restivus';
import { _ } from 'meteor/underscore';

import CRIMP from '../imports/settings';
import Messages from '../imports/data/messages';
import Events from '../imports/data/events';
import Categories from '../imports/data/categories';
import Teams from '../imports/data/teams';
import Climbers from '../imports/data/climbers';
import Scores from '../imports/data/scores';

Meteor.startup(() => {
  // TODO: Delete this crazy publication
  Meteor.publish('development', () => Meteor.users.find({}));
  Meteor.publish('messages', () => Messages.find({}));
  Meteor.publish('events', () => Events.find({}));
  Meteor.publish('categories', () => Categories.find({}));
  Meteor.publish('teams', () => Teams.find({}));
  Meteor.publish('climbers', () => Climbers.find({}));
  Meteor.publish('scores', () => Scores.find({}));
});

const Api = new Restivus({
  defaultHeaders: { 'Content-Type': 'application/json' },
  useDefaultAuth: true,
  prettyJson: true,
  auth: {
    token: 'services.resume.loginTokens.hashedToken',
    user: function authUser() {
      return {
        userId: this.request.headers['x-user-id'],
        token: Accounts._hashLoginToken(this.request.headers['x-auth-token']),
      };
    },
  },
});


Api.addRoute('judge/login', { authRequired: false }, {
  post: function postLogin() {
    // Ensure that judge is logging in from production-version of the app
    if (CRIMP.ENVIRONMENT.NODE_ENV === 'production' &&
        this.bodyParams.isProductionApp !== 'true') {
      return {
        statusCode: 400,
        body: { error: 'Missing/wrong isProductionApp in body' },
      };
    }

    if (!('fb_access_token' in this.bodyParams)) {
      return {
        statusCode: 400,
        body: { error: 'Missing fb_access_token in body' },
      };
    }

    // Call Facebook to authenticate the access token
    // HTTP.call is synchronous because callback is not provided
    const fbAccessToken = this.bodyParams.fb_access_token;
    let fbResponse;
    try {
      fbResponse = HTTP.call(
        'GET',
        `https://graph.facebook.com/v2.3/me?access_token=${fbAccessToken}`
       );
    } catch (e) {
      return {
        statusCode: 500,
        body: { error: e.message },
      };
    }

    // Clean up FB data to create user profile
    const options = _.pick(fbResponse.data, ['name']);
    let user;
    try {
      // Note: updateOrCreateUserFromExternalService is undocumented
      // See comment by n1mmy: https://github.com/meteor/meteor/issues/2648
      user = Accounts.updateOrCreateUserFromExternalService(
        'facebook',
        fbResponse.data,
        { profile: options });
    } catch (e) {
      return {
        statusCode: 500,
        body: {
          error: 'updateOrCreateUserFromExternalService has died :(',
        },
      };
    }

    // Retrieve user document of this user
    user = Meteor.users.findOne(user.userId);

    // Check for existing login tokens
    let hasExistingLogins = false;
    try {
      hasExistingLogins = user.services.resume.loginTokens.length > 0;
    } catch (e) { /* do nothing */ }

    // Do not issue new tokens if there are existing tokens and it is not
    // a force_login
    if (hasExistingLogins && this.bodyParams.force_login === 'false') {
      return {
        statusCode: 409,
        body: {
          error: 'Has an existing session in another device',
        },
      };
    }

    // Find out number of login counts of user
    let tokenCount = user.services.resume.loginTokensCount;
    tokenCount = typeof tokenCount === 'number'
      ? tokenCount + 1
      : 0;

    // Create login token and label the token
    const stampedToken = Accounts._generateStampedLoginToken();
    stampedToken.tokenNumber = tokenCount;

    Accounts._insertLoginToken(user._id, stampedToken);

    // Update user document for loginTokensCount
    Meteor.users.update(user._id, { $set: {
      'services.resume.loginTokensCount': tokenCount,
    } });


    const userRoles = Roles.getRolesForUser(user._id);
    // If it is a new user, set a default role
    if (userRoles.length < 1) {
      userRoles.push(CRIMP.ENVIRONMENT.DEMO_MODE ? 'admin' : 'pending');
      Roles.addUsersToRoles(user._id, userRoles);
    }

    // If this is the first user, give supreme privileges
    if (Meteor.users.find().count() === 1 &&
        !_.contains(userRoles, 'hukkataival')) {
      userRoles.push('hukkataival');
      Roles.addUsersToRoles(user._id, userRoles);
    }


    return {
      statusCode: 201,
      body: {
        'X-User-Id': user._id,
        'X-Auth-Token': stampedToken.token,
        remind_logout: hasExistingLogins,
        roles: userRoles,
        sequential_token: tokenCount,
      },
    };
  },
});


Api.addRoute('judge/logout', { authRequired: true }, {
  post: function postLogout() {
    const loginToken = this.request.headers['x-auth-token'];
    const hashed = Accounts._hashLoginToken(loginToken);

    Accounts.destroyToken(this.userId, hashed.hashedToken);

    return {
      statusCode: 200,
      body: {},
    };
  },
});


Api.addRoute('judge/categories', { authRequired: false }, {
  get: function getCategories() {
    const categoryDocs = Categories.find({}).fetch();
    const map = {
      _id: 'category_id',
      score_finalized: 'is_score_finalized',
    };

    categoryDocs.forEach((doc, index, array) => {
      const newDoc = {};
      _.each(doc, (value, key) => {
        const newkey = map[key] || key;
        newDoc[newkey] = value;
      });

      array[index] = newDoc;
    });

    return {
      statusCode: 200,
      body: { categories: categoryDocs },
    };
  },
});


Api.addRoute('judge/score', { authRequired: true }, {
  get: function getScore() {
    const options = this.queryParams;

    // const scoreSelector = {
    //   category_id: options.category_id,
    //   climber_id: options.climber_id,
    //   marker_id: options.marker_id,
    // };
    const cat = Categories.findOne({_id: options.category_id}) || Categories.findOne({});

    const climberDocs = Climbers.find({}).fetch();
    const newClimberDocs = [];
    climberDocs.forEach((doc, index) => {
      const newDoc = _.pick(doc, ['climber_name']);
      newDoc.climber_id = doc._id;
      newDoc.scores = [
        {
          'marker_id': 'NMQ00'+index,
          'category_id': cat._id,
          'route_id': cat.routes[0]._id || 'WmiYdjftrrBhiuzd9',
          'score': 'hello_we1zh1',
        },
        {
          'marker_id': 'NMQ00'+index,
          'category_id': cat._id,
          'route_id':  cat.routes[1].route_id || 'sFpauqFGuxDeCwDz7',
          'score': '11B',
        },
        {
          'marker_id': 'NMQ00'+index,
          'category_id': cat._id,
          'route_id': '6GW2eLgpNE2Ad2RrA',
          'score': '11B11T'
        },
        {
          'marker_id': 'NMQ00'+index,
          'category_id': cat._id,
          'route_id': 'v3fNAhZ79Med5cfLW',
          'score': 'B11T'
        },
        {
          'marker_id': 'NMQ00'+index,
          'category_id': cat._id,
          'route_id': 'v3fNAhZ79Med5cfLW',
          'score': 'T11T'
        },
        {
          'marker_id': 'NMQ00'+index,
          'category_id': cat._id,
          'route_id': 'v3fNAhZ79Med5cfLW',
          'score': 'T11T'
        },
      ]

      newClimberDocs.push(newDoc);
    });


    // const scoreDocs = Scores.find(scoreSelector).fetch();
    return {
      statusCode: 200,
      body: { climber_scores: newClimberDocs },
    };
  },
});


Api.addRoute('judge/score/:route_id/:marker_id', { authRequired: true }, {
  post: function postScore() {
    const options = this.urlParams;

    const cat = Categories.findOne({ 'routes._id': options.route_id });
    // const scs = Scores.findOne({
    //               marker_id: );
    // console.log(cat);

    return {
      statusCode: 501,
      body: {
        "climber_id": '123123',
        "climber_name": 'CATERPIE',
        "category_id": cat._id,
        "route_id": options.route_id,
        "marker_id": "abc007",
        "score": "11B11T"
      },
    };
  },
});


Api.addRoute('judge/helpme', { authRequired: true }, {
  post: function postHelpMe() {
    return {
      "X-User-Id": this.userId,
      "X-Auth-Token": this.user.services.facebook.name,
      "route_id": this.bodyParams.route_id,
    }

    // return {
    //   statusCode: 501,
    //   body: { error: 'Not implemented (yet)' },
    // };
  },
});


Api.addRoute('judge/report', { authRequired: true }, {
  post: function postReport() {

    const options = this.bodyParams;
    if (options.blocked === 'true' && options.force === 'false') {
      return {
        "X-User-Id": '123123',
        "user_name": 'CATERPIE',
        "category_id": options.category_id,
        "route_id": options.route_id,
      }
    } else {
      return {
        "X-User-Id": this.userId,
        "user_name": this.user.services.facebook.name,
        "category_id": options.category_id,
        "route_id": options.route_id,
      }
    }


    // return {
    //   statusCode: 501,
    //   body: { error: 'Not implemented (yet)' },
    // };
  },
});


Api.addRoute('judge/setactive', { authRequired: true }, {
  put: function putSetActive() {
    return {
      "route_id": this.bodyParams.route_id,
      "marker_id": this.bodyParams.marker_id,
      "climber_id": "123123",
      "climber_name": "caterpie",
    }

    // return {
    //   statusCode: 501,
    //   body: { error: 'Not implemented (yet)' },
    // };
  },
});


Api.addRoute('judge/clearactive', { authRequired: true }, {
  put: function putClearActive() {
    return {
      "route_id": this.bodyParams.route_id,
      "marker_id": "",
      "climber_id": "",
      "climber_name": "",
    }
    // return {
    //   statusCode: 501,
    //   body: { error: 'Not implemented (yet)' },
    // };
  },
});


// TODO: Remove latency endpoint
Api.addRoute('test/latency', { authRequired: true }, {
  get: () => {
    // simulate latency
    Meteor._sleepForMs(10000);

    const msg = {
      method: 'GET',
    };

    const insertStatus = Messages.methods.insert.call({
      payload: msg,
    });

    return insertStatus;
  },
});

