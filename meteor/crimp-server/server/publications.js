Meteor.publish('categories', function() {
  return Categories.find({});
});

Meteor.publish('climbers', function() {
  return Climbers.find({});
});
