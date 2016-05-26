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
import HelpMe from '../imports/data/helpme';
import ActiveTracker from '../imports/data/activetracker';

// TODO: REMOVE seedDatabase. DEV TESTING ONLY.
import seedDatabase from '../imports/seedDatabase';


Meteor.startup(() => {
  // TODO: Delete this crazy publication
  Meteor.publish('development', () => Meteor.users.find({}));
  Meteor.publish('messages', () => Messages.find({}));
  Meteor.publish('events', () => Events.find({}));
  Meteor.publish('categories', () => Categories.find({}));
  Meteor.publish('teams', () => Teams.find({}));
  Meteor.publish('climbers', () => Climbers.find({}));
  Meteor.publish('scores', () => Scores.find({}));
  Meteor.publish('helpme', () => HelpMe.find({}));
  Meteor.publish('activetracker', () => ActiveTracker.find({}));
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
    const picked = [
      '_id',
      'category_name',
      'acronym',
      'is_score_finalized',
      'time_start',
      'time_end',
      'routes',
    ];
    const map = {
      _id: 'category_id',
      score_finalized: 'is_score_finalized',
    };

    // Go through each Category doc to rename/discard keys
    categoryDocs.forEach((doc, index, array) => {
      const truncatedDoc = _.pick(doc, picked);
      const mappedDoc = {};

      // Rename the DB keys to conform with API spec
      _.each(truncatedDoc, (value, key) => {
        const newkey = map[key] || key;
        mappedDoc[newkey] = value;
      });

      // Go through each route to generate score_rules
      (mappedDoc.routes).forEach((route) => {
        let scoreRules = doc.score_system;

        // TODO: Use the scoreSystem object to generate this
        // This is suitable for now because points is the only system that
        // uses this, but eventually we have to extend it for more systems
        if (route.score_rules.points) {
          scoreRules += `__${route.score_rules.points}`;
        }

        route.score_rules = scoreRules;
      });

      array[index] = mappedDoc;
    });

    return {
      statusCode: 200,
      body: { categories: categoryDocs },
    };
  },
});


Api.addRoute('judge/score', { authRequired: true }, {
  get: function getScore() {
    const allowedOptions = ['event_id',
                            'category_id',
                            'route_id',
                            'climber_id',
                            'marker_id'];
    const options = _.pick(this.queryParams, allowedOptions);
    const scoreSelector = {};

    const targetCategories = [];

    // Event is not stored inside a Score document, hence we need to
    // do a look-up to get all the Categories inside an Event
    if (options.event_id) {
      Categories
        .find({ 'event._id': options.event_id })
        .fetch()
        .forEach((category) => {
          targetCategories.push(category._id);
        });

      scoreSelector.category_id = { $in: targetCategories };
    }


    if (options.category_id) {
      // If Event and Category is specified, check if Category is
      // child of the Event
      if (targetCategories.length
          && !targetCategories.includes(options.category_id)) {
        throw new Meteor.Error('CategoryNotLinkedToEvent');
      } else {
        scoreSelector.category_id = options.category_id;
      }
    }

    // Note: route_id is not checked against the Categories
    if (options.route_id) {
      scoreSelector.scores = { $elemMatch: {
        route_id: options.route_id,
      } };
    }

    if (options.climber_id) {
      scoreSelector.climber_id = options.climber_id;
    }

    if (options.marker_id) {
      scoreSelector.marker_id = options.marker_id;
    }

    const targetScores = Scores.find(scoreSelector);

    if (targetScores.count() === 0) {
      return {
        statusCode: 200,
        body: { climber_scores: [] },
      };
    }

    // Get set of climbers affected by operation
    let targetClimbers = [];
    targetScores.fetch().forEach((score) => {
      targetClimbers.push(score.climber_id);
    });
    targetClimbers = Climbers.find({
      _id: { $in: targetClimbers },
    }).fetch();


    const map = { _id: 'climber_id' };
    targetClimbers.forEach((climber, index, array) => {
      const truncatedDoc = _.pick(climber, ['_id', 'climber_name']);
      const mappedDoc = {};

      _.each(truncatedDoc, (value, key) => {
        const newkey = map[key] || key;
        mappedDoc[newkey] = value;
      });

      array[index] = mappedDoc;
    });


    const scoreOutput = [];
    targetScores
      .fetch()
      .forEach((scoreDoc) => {
        for (let i = targetClimbers.length - 1; i >= 0; i--) {
          if (scoreDoc.climber_id === targetClimbers[i].climber_id) {
            const allScores = [];

            for (let j = scoreDoc.scores.length - 1; j >= 0; j--) {
              const singleScore = {};
              singleScore.marker_id = scoreDoc.marker_id;
              singleScore.category_id = scoreDoc.category_id;
              singleScore.route_id = scoreDoc.scores[j].route_id;
              singleScore.score = scoreDoc.scores[j].score_string;


              if (options.route_id) {
                if (options.route_id === singleScore.route_id) {
                  allScores.push(singleScore);
                }
              } else {
                allScores.push(singleScore);
              }
            }

            targetClimbers[i].scores = allScores;
            scoreOutput.push(targetClimbers[i]);
          }
        }
      });

    return {
      statusCode: 200,
      body: { climber_scores: scoreOutput },
    };
  },
});


Api.addRoute('judge/score/:route_id/:marker_id', { authRequired: false }, {
  post: function postScore() {
    const options = this.urlParams;
    const scoreString = this.bodyParams.score_string;

    /**
     * TODO: Implement the magic sequencing with sequential token
     */

    let targetScore = Scores.find({
      marker_id: options.marker_id,
      scores: { $elemMatch: {
        route_id: options.route_id,
      } },
    });

    if (targetScore.count() === 0) {
      throw new Meteor.Error('RouteOrMarkerError');
    } else if (targetScore.count() > 1) {
      throw new Meteor.Error('SelectedMultipleScoresForUpdate');
    }

    // There will only be 1 Score document fetched
    targetScore = targetScore.fetch()[0];

    // Find the Score of the target Route
    const scoreDoc = _.find(targetScore.scores,
                            (doc) => (doc.route_id === options.route_id));

    // Append latest Score to the existing String
    const newScoreString = `${scoreDoc.score_string}${scoreString}`;


    Scores.update({
      marker_id: options.marker_id,
      'scores.route_id': options.route_id,
    }, {
      $set: {
        'scores.$.score_string': newScoreString,
      },
    });


    return {
      statusCode: 501,
      body: {
        climber_id: targetScore.climber_id,
        category_id: targetScore.category_id,
        route_id: options.route_id,
        marker_id: targetScore.marker_id,
        score: newScoreString,
      },
    };
  },
});


Api.addRoute('judge/helpme', { authRequired: true }, {
  post: function postHelpMe() {
    const targetRouteId = this.bodyParams.route_id;
    const targetCategory = Categories.findOne({
      routes: { $elemMatch: {
        _id: targetRouteId,
      } },
    });
    const targetRoute = _.find(targetCategory.routes,
                               (route) => (route._id === targetRouteId));

    // Build the document for HelpMe
    const helpMeDoc = {
      route_id: targetRouteId,
      route_name: targetRoute.route_name,
      category_name: targetCategory.category_name,
      user_id: this.userId,
      user_name: this.user.services.facebook.name,
    };

    // Add a dummy callback function so the op does not block
    HelpMe.insert(helpMeDoc, () => {});

    return {
      statusCode: 200,
      body: {},
    };
  },
});


Api.addRoute('judge/report', { authRequired: true }, {
  post: function postReport() {
    const options = this.bodyParams;
    const targetActive = ActiveTracker.findOne({ route_id: options.route_id });

    if (targetActive
        && options.force !== 'true') {
      return {
        statusCode: 200,
        body: {
          'X-User-Id': targetActive.user_id,
          user_name: targetActive.user_name,
          category_id: targetActive.category_id,
          route_id: targetActive.route_id,
        },
      };
    }

    const targetCategory = Categories.findOne({
      category_id: options.category_id,
      routes: { $elemMatch: {
        _id: options.route_id,
      } },
    });

    const targetRoute = _.find(targetCategory.routes,
                             (route) => (route._id === options.route_id));

    // Add a dummy callback function so the op does not block
    ActiveTracker.insert({
      route_id: options.route_id,
      route_name: targetRoute.route_name,
      category_id: targetCategory._id,
      category_name: targetCategory.category_name,
      user_id: this.userId,
      user_name: this.user.services.facebook.name,
    }, () => {});

    return {
      statusCode: 200,
      body: {
        'X-User-Id': this.userId,
        user_name: this.user.services.facebook.name,
        category_id: targetCategory._id,
        route_id: options.route_id,
      },
    };
  },
});


Api.addRoute('judge/setactive', { authRequired: true }, {
  put: function putSetActive() {
    const options = this.bodyParams;
    const targetActive = ActiveTracker.findOne({ route_id: options.route_id });
    const targetScore = Scores.findOne({
      marker_id: options.marker_id,
      scores: { $elemMatch: { route_id: options.route_id } },
    });
    const targetClimber = Climbers.findOne({ _id: targetScore.climber_id });

    if (targetActive) {
      // Existing ActiveTracker. Include an update of the judge name to
      // ensure that it is current
      ActiveTracker.update({
        route_id: options.route_id,
      }, { $set: {
        user_id: this.userId,
        user_name: this.user.services.facebook.name,
        climber_id: targetScore.climber_id,
        marker_id: targetScore.marker_id,
        climber_name: targetClimber.climber_name,
      } }, () => {});
    } else {
      // No existing ActiveTracker. Possibly that the timer deleted it.
      // Hence, we would recreate the ActiveTracker document.
      const targetCategory = Categories.findOne({
        routes: { $elemMatch: {
          _id: options.route_id,
        } },
      });

      const targetRoute = _.find(targetCategory.routes,
                               (route) => (route._id === options.route_id));

      // Add a dummy callback function so the op does not block
      ActiveTracker.insert({
        route_id: targetRoute._id,
        route_name: targetRoute.route_name,
        category_id: targetCategory._id,
        category_name: targetCategory.category_name,
        user_id: this.userId,
        user_name: this.user.services.facebook.name,
        climber_id: targetScore.climber_id,
        marker_id: targetScore.marker_id,
        climber_name: targetClimber.climber_name,
      }, () => {});
    }

    return {
      statusCode: 200,
      body: {},
    };
  },
});


Api.addRoute('judge/clearactive', { authRequired: true }, {
  put: function putClearActive() {
    const options = this.bodyParams;
    const targetActive = ActiveTracker.findOne({ route_id: options.route_id });

    if (targetActive) {
      // Existing ActiveTracker. Include an update of the judge name to
      // ensure that it is current
      ActiveTracker.update({
        route_id: options.route_id,
      }, { $set: {
        user_id: this.userId,
        user_name: this.user.services.facebook.name,
        climber_id: '',
        marker_id: '',
        climber_name: '',
      } }, {
        removeEmptyStrings: false,
      }, () => {});
    } else {
      // No existing ActiveTracker. Possible that the timer deleted it.
      // Hence, we would recreate the ActiveTracker document.
      const targetCategory = Categories.findOne({
        routes: { $elemMatch: {
          _id: options.route_id,
        } },
      });

      const targetRoute = _.find(targetCategory.routes,
                               (route) => (route._id === options.route_id));

      // Add a dummy callback function so the op does not block
      ActiveTracker.insert({
        route_id: targetRoute._id,
        route_name: targetRoute.route_name,
        category_id: targetCategory._id,
        category_name: targetCategory.category_name,
        user_id: this.userId,
        user_name: this.user.services.facebook.name,
      }, () => {});
    }

    return {
      statusCode: 200,
      body: {},
    };
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
