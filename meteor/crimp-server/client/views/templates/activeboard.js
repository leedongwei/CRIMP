Template.activeboard.onCreated(function() {
  Tracker.autorun(function() {
    Meteor.subscribe('getActiveClimbers');
  });
});

Template.activeboard.helpers({
  // TODO: Use Meteor.call to replace .find()
  activeClimbers: function() {
    return ActiveClimbers.find({}, { sort: ['route_id', 'asc'] })
                         .fetch();
  }
});