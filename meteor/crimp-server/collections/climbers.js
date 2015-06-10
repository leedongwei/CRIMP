Climbers = new Mongo.Collection('climbers');
Schema.Climber = new SimpleSchema({
  name: {
    label: 'Name of climber',
    type: String
  },
  category_id: {
    label: 'Category of climber',
    type: String,
    min: 3,
    max: 3
  },
  number: {
    label: 'Climber\'s number',
    type: String,
    regEx: /\d+/,
    min: 3,
    max: 3
  },
  id: {
    label: 'Climber ID (category_id + number)',
    type: String,
    index: true,
    unique: true,
    min: 6,
    max: 6
  },
  // TODO: Probably not needed. Can delete
  // scores: {
  //   label: 'Scores on all routes',
  //   type: String,
  //   optional: true
  // },
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
  }
});


Climbers.attachSchema(Schema.Climber);

// TODO: Ensure admin-only access
Meteor.methods({
  addClimber: function(data) {

    // Create the ID
    data['id'] = data.category_id + data.number;

    Climbers.insert(data, function(error, insertedId) {
      if (error) {
        // TODO: handle the error
        console.log('dongwei: got into an error')
        console.log(error);

        return error;
      } else {
        console.log('dongwei: we are doing just fine')
        return insertedId;
      }
    });
  },

  findClimber: function(data) {
    return Climbers.find(data).fetch();
  },

  updateClimber: function(data) {

    data['id'] = data.category + data.number;
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