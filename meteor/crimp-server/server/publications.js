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