Climbers = new Mongo.Collection('climbers');
Schema.Climber = new SimpleSchema({
  name: {
    label: "Name of climber",
    type: String
  },
  category: {
    label: "Acronym of category of climber",
    type: String
  },
  number: {
    label: "Climber number (Combine with Category for ID)",
    type: String,
    index: true,
    unique: true,
    // regEx: /\d/g,
    min: 3,
    max: 3
  },
  scores: {
    // TODO: Change to ObjectID reference
    label: "Scores on all routes",
    type: String
  },
  affliation: {
    label: "Affliations of the climber (school, gym etc)",
    type: String,
    optional: true
  },
  scores_tiebreak: {
    label: "Manually rank climbers that have equal score",
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
    label: "1st/2nd/3rd/Qualified",
    optional: true
  },
  additional_status: {
    type: String,
    label: 'Additional comment to reflect on scoreboard',
    optional: true
  }
});


Climbers.attachSchema(Schema.Climber);

// TODO: Ensure admin-only access
Meteor.methods({
  createClimber: function(data) {
    Climbers.insert(data, function(error, insertedId) {
      if (error) {
        // TODO: handle the error
        console.log(error);

        return error;
      } else {
        return insertedId;
      }
    });
  },

  findClimber: function(data) {
    return Climbers.find(data).fetch();
  },

  updateClimber: function(data) {

    Climbers.update(selector, modifier, function(error, updatedCount) {
      if (error) {
        // TODO: handle the error
        console.log(error);

        return error;
      } else {
        return updatedCount;
      }
    });
  },

  deleteClimber: function(data) {
    Climbers.remove(data, function(error, removedCount) {
      if (error) {
        // TODO: handle the error
        console.log(error);


        return error;
      } else {
        return removedCount;
      }
    });
  }
});