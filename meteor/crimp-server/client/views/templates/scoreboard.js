Template.scoreboard.created = function() {
  Meteor.subscribe('getCategories');
  Meteor.subscribe('getClimbers', currentCategory);
}

Template.scoreboard.helpers({

  // TODO: Use Meteor.call to replace .find()

  categories: function() {
    return Categories.find({}).fetch();
  },
  climbers: function() {
    currentCategoryDeps.depend();
    // TODO: See below
    var climbers = Climbers.find(currentCategory).fetch();
    var scores = Scores.find({}).fetch();

    console.log(climbers);
    console.log(scores);

    return Climbers.find(currentCategory).fetch();
  },
  scores: function(climber_id) {
    return Scores.find({}).fetch();
  }
});

Template.scoreboard.events({
  'change .scoreboard-categories': function(event, template) {

    // TODO: Set this up after Climber Collection
    currentCategory = { category_id: event.target.value };
    currentCategoryDeps.changed();
  }
});

