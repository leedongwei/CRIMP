/**
 *
 *  Spectator-scoreboard publications
 *
 */

Meteor.publish('getActiveClimbers', () => {
  return ActiveMonitor.find({}, {
    fields: {
      route_id: 1,
      climber_id: 1,
      climber_name: 1
    }
  });
});

Meteor.publish('getCategories', () => {
  return Categories.find({}, {
    fields: {
      _id: 1,
      category_name: 1,
      category_id: 1,
      scores_finalized: 1,
      time_start: 1,
      time_end: 1
    }
  });
});

/**
 *  @param
 *    {string} category - category_id of any category
 */
Meteor.publish('getClimbers', (category) => {
  if (category) {
    return Climbers.find(category, {
      fields: {
        number: 0
      }
    });
  }

  return;
});

/**
 *  @param
 *    {string} category - category_id of any category
 */
Meteor.publish('getScores', (category) => {
  if (category) {
    return Scores.find(category, {
      fields: {
        category_id: 0,
        unique_id: 0,
        admin_id: 0,
        updated_at: 0
      }
    });
  }

  return;
});



/**
 *
 *  Admin-exclusive publications
 *
 *  Note: Do not use ES6 arrow functions for publications which require the
 *  use of this.userId to get user identity.
 *
 *  Refer to https://github.com/babel/babel/issues/730
 */

/**
 *  Admin publication for ActiveMonitor that exposes the ActiveAdmins too
 */
Meteor.publish('adminActiveMonitor', function() {
  if (Roles.userIsInRole(this.userId, CRIMP.roles.trusted)) {
    return ActiveMonitor.find({});
  }

  return;
});

/**
 *  Users refers to the admin accounts, not the climbers
 */
Meteor.publish('adminAllUsers', function() {
  if (Roles.userIsInRole(this.userId, CRIMP.roles.trusted)) {
    return Meteor.users.find({}, {
      fields: {
        roles: 1,
        profile: 1
      }
    });
  }

  return;
});

/**
 *  Admin version of 'getScore' publication above that exposes more
 *  data from the Score document
 *
 *  @param
 *    {string} category - category_id of any category
 */
Meteor.publish('adminScores', function(category) {
  if (Roles.userIsInRole(this.userId, CRIMP.roles.trusted) &&
      category) {
    return Scores.find(category);
  }

  return;
});

Meteor.publish('adminRecentScores', function() {
  if (Roles.userIsInRole(this.userId, CRIMP.roles.trusted)) {
    return RecentScores.find({});
  }

  return;
});
