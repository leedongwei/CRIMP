Scores = new Mongo.Collection('score');
Schema.Score = new SimpleSchema({
  climber_id: {
    label: 'ID of climber',
    type: String,
    min: 6,
    max: 6,
    denyUpdate: true
  },
  admin_id: {
    // TODO: Find a more efficient way of referencing admins
    label: 'ID of admin',
    type: String
  },
  category_id: {
    label: 'ID of category',
    type: String,
    min: 3,
    max: 3,
    denyUpdate: true
  },
  route_id: {
    label: 'ID of route',
    type: String,
    min: 6,
    max: 6,
    denyUpdate: true
  },
  score_string: {
    label: 'Raw scoring string',
    type: String
  },
  score_top: {
    label: 'Attempts to top',
    type: Number
  },
  score_bonus: {
    label: 'Attempts to bonus',
    type: Number
  },
  updated_at: {
    type: Date,
    autoValue: function() {
      return new Date();
    },
    denyInsert: true,
    optional: true
  }
});


Scores.attachSchema(Schema.Score);

// TODO: Ensure admin-only access
Meteor.methods({
  findScore: function(data) {
    return Scores.find(data).fetch();
  },

  updateScore: function(data) {
    var selector = {
      route_id: data['route_id'],
      climber_id: data['climber_id']
    };

    // TODO: Tabulate top/bonus before saving

    delete data['route_id'];
    delete data['climber_id'];

    Scores.upsert(selector, data, function(error, updatedCount) {
      if (error) {
        // TODO: handle the error
        console.log(error);

        return error;
      } else {
        return updatedCount;
      }
    });
  },

  deleteScore: function(data) {
    // TODO: This is probably not needed
  }
});