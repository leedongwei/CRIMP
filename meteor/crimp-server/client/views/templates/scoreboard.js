Template.scoreboard.onCreated(function() {
  Tracker.autorun(function() {
    Meteor.subscribe('getCategories');
    Meteor.subscribe('getClimbers',
                     { category_id: Session.get('currentCategory') });
    Meteor.subscribe('getScores',
                     { category_id: Session.get('currentCategory') });
  });
});

Template.scoreboard.helpers({
  // Moved to individual categories
});



Template.scoreboard_categories.helpers({
  // TODO: Use Meteor.call to replace .find()
  categories: function() {
    return Categories.find({}).fetch();
  },
  selectedCategory: function(category_id) {
    return category_id === Session.get('currentCategory');
  }
});

Template.scoreboard_categories.events({
  'change .scoreboard-categories': function(event, template) {
    Session.set('currentCategory', event.target.value);
  }
});




Template.scoreboard_climber.helpers({
  climbers: function() {
    return Climbers
            .find({ category_id: Session.get('currentCategory') })
            .fetch();
  },
  scores: function() {

  }
});