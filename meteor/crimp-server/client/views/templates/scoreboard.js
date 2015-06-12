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
  isCurrentCategory: function(category_id) {
    return Session.equals('currentCategory', category_id);
  }
});

Template.scoreboard_categories.events({
  'change .scoreboard-categories': function(event, template) {
    Session.set('currentCategory', event.target.value);
  }
});




Template.scoreboard_climber.helpers({
  climbers: function() {
    // console.log(Climbers.find({'climber_id': 'NMQ001'}).fetch());
    return Climbers.find({ category_id: Session.get('currentCategory') })
                   .fetch();
  },
  aggregatedScores: function(scores) {
    var data = {
      routes: scores,
      total_top: 0,
      total_bonus: 0
    };

    // console.log(data)
    return data;
  }
});