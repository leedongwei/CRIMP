Meteor.publish('getCategories', function() {
  return Categories.find({});
});

Meteor.publish('getClimbers', function(category) {
  category = typeof category !== 'undefined' ? category : {};
  return Climbers.find(category);
});

Meteor.publish('getScores', function(category) {
  category = typeof category !== 'undefined' ? category : {};
  return Scores.find(category);
});
