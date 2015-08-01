// Spectator scoreboard publications
Meteor.publish('getCategories', function() {
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

Meteor.publish('getClimbers', function(category) {
  if (category) {
    return Climbers.find(category, {
      fields: {
        number: 0
      }
    });
  }

  return;
});

Meteor.publish('getScores', function(category) {
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

Meteor.publish('getActiveClimbers', function() {
  return ActiveClimbers.find({}, {
    fields: {
      route_id: 1,
      climber_id: 1,
      climber_name: 1
    }
  });
});



// Admin-exclusive publications
Meteor.publish('adminActiveClimbers', function() {
  if (Roles.userIsInRole(this.userId, CRIMP.roles.trusted)) {
    return ActiveClimbers.find({});
  }

  return;
});

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
