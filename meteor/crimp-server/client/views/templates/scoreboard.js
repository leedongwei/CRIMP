Template.scoreboard.helpers({
  categories: function() {
    return Categories.find({});
  },
  climbers: function() {
    currentCategoryDeps.depend();
    // TODO: See below
    return Climbers.find(currentCategory);
  }
});

Template.scoreboard.events({
  'change .scoreboard-categories': function(event, template) {

    // TODO: Set this up after Climber Collection
    currentCategory = { category_id: event.target.value };
    currentCategoryDeps.changed();
  }
});

