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
    var climberData = Climbers
                        .find({ category_id: Session.get('currentCategory') })
                        .fetch();

    climberData.forEach(function(climber, index) {

      var scoreArray = [];
      for (var i=1; i < Object.keys(climber.scores).length+1; i++) {
        scoreArray.push(Scores.findOne(climber.scores[i]));
      }
      climber.scores = scoreArray;

      console.log(climber);
      console.log('   --**--   ')
    });

    return climberData;
  }
});