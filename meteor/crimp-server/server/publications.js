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
  category = typeof category !== 'undefined' ? category : {};
  return Climbers.find(category, {
    fields: {
      number: 0
    }
  });
});

Meteor.publish('getScores', function(category) {
  category = typeof category !== 'undefined' ? category : {};
  return Scores.find(category, {
    fields: {
      category_id: 0,
      unique_id: 0,
      admin_id: 0,
      updated_at: 0
    }
  });
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



// ADMIN-ONLY PUBLICATIONS
Meteor.publish('adminActiveClimbers', function() {
  if (Roles.userIsInRole(this.userId, CRIMP.roles.trusted)) {
    return ActiveClimbers.find({});
  } else {
    this.stop();
    return;
  }
});

Meteor.publish('adminPendingJudges', function() {
  if (Roles.userIsInRole(this.userId, CRIMP.roles.trusted)) {
    return Roles.getUsersInRole('pending');
  } else {
    this.stop();
    return;
  }
});

Meteor.publish('adminAllScores', function(category) {
  category = typeof category !== 'undefined' ? category : {};
  return Scores.find(category);
});

Meteor.publish('adminRecentScores', function() {
  if (Roles.userIsInRole(this.userId, CRIMP.roles.trusted)) {
    return RecentScores.find({});
  } else {
    this.stop();
    return;
  }
});

Meteor.publish('adminAllUsers', function() {
  if (Roles.userIsInRole(this.userId, CRIMP.roles.trusted)) {
    return Meteor.users.find({}, {
      fields: {
        roles: 1,
        profile: 1
      }
    });
  } else {
    this.stop();
    return;
  }
});
