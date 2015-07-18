Template.activeboard.onCreated(function() {
  Tracker.autorun(function() {
    Meteor.subscribe('getActiveClimbers');
  });
});

Template.activeboard.helpers({
  activeClimbers: function() {
    return ActiveClimbers.find({}, { sort: ['route_id', 'asc'] })
                         .fetch();
  },
  nextCategoryTime: function() {
    return 'No idea';
  }
});