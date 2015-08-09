Template.activeboard.onCreated(function() {
  Meteor.subscribe('getActiveClimbers');
});

Template.activeboard.helpers({
  activeClimbers: function() {
    return ActiveMonitor.find({}, { sort: ['route_id', 'asc'] })
                        .fetch();
  },
  nextCategory: function() {
    return '';
  },
  nextCategoryTime: function() {
    return '';
  }
});