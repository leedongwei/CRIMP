import { Meteor } from 'meteor/meteor';
import { HTTP } from 'meteor/http';
import { Accounts } from 'meteor/accounts-base';
import { Restivus } from 'meteor/nimble:restivus';
import { _ } from 'meteor/underscore';

import { Messages } from '../imports/data/messages';
import Events from '../imports/data/events.js';
import Categories from '../imports/data/categories.js';


Meteor.startup(() => {
  // code to run on server at startup
});

const Api = new Restivus({
  defaultHeaders: { 'Content-Type': 'application/json' },
  useDefaultAuth: false,
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

Api.addRoute('test', {
  get: () => {
    const msg = {
      method: 'GET',
    };

    const insertStatus = Messages.methods.insert.call({
      payload: msg,
    });

    // simulate latency
    Meteor._sleepForMs(5000);

    return insertStatus;
  },
  post: () => {
    const msg = {
      method: 'GET',
      body: this.bodyParams,
    };

    const insertStatus = Messages.methods.insert.call({
      payload: msg,
    });

    return insertStatus;
  },
});

Api.addRoute('judge/login', {
  post: function postfunc() {
    // Ensure that judge is logging in from production-version of the app
    if (ENVIRONMENT.NODE_ENV === 'production' &&
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

    // Clean up FB data to create profile link
    const options = _.pick(fbResponse.data, ['name', 'link']);

    // Note: updateOrCreateUserFromExternalService is undocumented
    // See comment by n1mmy: https://github.com/meteor/meteor/issues/2648
    let userCreated;
    try {
      userCreated = Accounts.updateOrCreateUserFromExternalService(
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

    const stampedToken = Accounts._generateStampedLoginToken();
    const hashStampedToken = Accounts._hashStampedToken(stampedToken);

    Accounts._insertLoginToken('123', stampedToken);

    // console.log(Meteor.users.update(userCreated.userId,
    //   { $push: {
    //     services: [{
    //       resume: {
    //         loginTokens: hashStampedToken,
    //       },
    //     }],
    //   } }
    // ));

    return {
      statusCode: 201,
      body: {
        'X-User-Id': userCreated.userId,
        'X-Auth-Token': stampedToken.token,
        roles: '',
      },
    };
  },
});
