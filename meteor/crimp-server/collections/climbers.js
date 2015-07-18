Climbers = new Mongo.Collection('climbers');
CRIMP.schema.climber = new SimpleSchema({
  climber_name: {
    label: 'Name of climber',
    type: String
  },
  category_id: {
    label: 'Category of climber',
    type: String,
    min: 3,
    max: 3,
    denyUpdate: true
  },
  number: {
    label: 'Climber\'s number',
    type: String,
    regEx: /\d+/,
    min: 3,
    max: 3,
    denyUpdate: true
  },
  climber_id: {
    label: 'Climber ID (category_id + number)',
    type: String,
    index: true,
    unique: true,
    min: 6,
    max: 6,
    denyUpdate: true
  },
  scores: {
    label: 'List to _id of scoring documents',
    type: Object,
    blackbox: true
  },
  affliation: {
    label: 'Affliations of the climber (school, gym etc)',
    type: String,
    optional: true
  },
  scores_tiebreak: {
    label: 'Manually rank climbers that have equal score',
    type: Number,
    autoValue: function() {
      if (this.isInsert) {
        return 1;
      }
    },
    optional: true
  },
  standard_status: {
    type: String,
    label: '1st/2nd/3rd/Qualified',
    optional: true
  },
  additional_status: {
    type: String,
    label: 'Additional comment to reflect on scoreboard',
    optional: true
  },
  // identity: {
  //   type: String,
  //   label: 'NRIC or driver license number',
  //   optional: true
  // },
  // notes: {
  //   type: String,
  //   label: 'Notes on climber (i.e. payment status) that will be hidden',
  //   optional: true
  // }
});


Climbers.attachSchema(CRIMP.schema.climber);

Meteor.methods({
  addClimber: function(data) {
    if (!Roles.userIsInRole(Meteor.user(), CRIMP.roles.trusted)) {
      throw new Meteor.Error(403, "Access denied");
    }

    var scores = {},
        routeCount = Categories
                      .find({'category_id': data.category_id})
                      .fetch()[0].route_count;
    data.climber_id = data.category_id + data.number;

    // Step 1: Create the X number of scoring documents associated with
    // the climber
    for (var i=1; i < routeCount+1; i++) {
      var scoreDocument = {
        climber_id: data.climber_id,
        admin_id: 'autoCreated',
        category_id: data.category_id,
        route_id: data.category_id + i,
        unique_id: data.climber_id + data.category_id + i,
        score_string: '',
        score_top: 0,
        score_bonus: 0
      }

      scores[i] = Scores.insert(scoreDocument,
                    { removeEmptyStrings: false, autoConvert: false },
                    function(error, result) {
        if (error)  throw error;
      });

    }

    // Step 2: Append score documents to the climber document
    data.scores = scores;

    // Step 3: Insert climber document
    return Climbers.insert(data, function(error, results) {
      // TODO: Handle error
    });

  },

  findClimber: function(data) {
    return Climbers.find(data).fetch();
  },

  updateClimber: function(data) {
    if (!Roles.userIsInRole(Meteor.user(), CRIMP.roles.trusted)) {
      throw new Meteor.Error(403, "Access denied");
    }

    return Climbers.update(selector, modifier, function(error, updatedCount) {
      if (error)  throw error;
    });
  },

  deleteClimber: function(data) {
    // Climbers.remove(data, function(error, removedCount) {
    //   if (error)  throw error;
    // });

    // TODO: Delete score documents when deleting climber
  }
});