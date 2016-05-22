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
        body: { error: 'Missing/wrong "isProductionApp" value in body' },
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
    if (hasExistingLogins && !this.bodyParams.force_login) {
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


Api.addRoute('judge/logout', { authRequired: false }, {
  post: function postLogout() {
    return {
      statusCode: 501,
      body: { error: 'Not implemented (yet)' },
    };
  },
});


Api.addRoute('judge/categories', { authRequired: false }, {
  get: function getCategories() {
    return {
      statusCode: 501,
      body: { error: 'Not implemented (yet)' },
    };
  },
});


Api.addRoute('judge/score', { authRequired: false }, {
  get: function getScore() {
    return {
      statusCode: 501,
      body: { error: 'Not implemented (yet)' },
    };
  },
});


Api.addRoute('judge//score/:route_id/:climber_id', { authRequired: false }, {
  post: function postScore() {
    return {
      statusCode: 501,
      body: { error: 'Not implemented (yet)' },
    };
  },
});


Api.addRoute('judge/report', { authRequired: false }, {
  post: function postReport() {
    return {
      statusCode: 501,
      body: { error: 'Not implemented (yet)' },
    };
  },
});


Api.addRoute('judge/helpme', { authRequired: false }, {
  post: function postHelpMe() {
    return {
      statusCode: 501,
      body: { error: 'Not implemented (yet)' },
    };
  },
});


Api.addRoute('judge/setactive', { authRequired: false }, {
  put: function putSetActive() {
    return {
      statusCode: 501,
      body: { error: 'Not implemented (yet)' },
    };
  },
});


Api.addRoute('judge/clearactive', { authRequired: false }, {
  put: function putClearActive() {
    return {
      statusCode: 501,
      body: { error: 'Not implemented (yet)' },
    };
  },
});


// TODO: Remove latency endpoint
Api.addRoute('test/latency', { authRequired: false }, {
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

