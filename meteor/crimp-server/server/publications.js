Meteor.publish('getCategories', function() {
  return Categories.find({});
});

Meteor.publish('getClimbers', function(category_id) {
  check(category_id, String);
  return Climbers.find({});
});

Meteor.publish('scores', function() {
  return Scores.find({});
});
