Scores = new Mongo.Collection('scores');
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
    denyUpdate: true
  },
  score_string: {
    label: 'Raw scoring string',
    type: String,
    trim: false
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
    optional: true
  }
});


Scores.attachSchema(Schema.Score);

// TODO: Ensure admin-only access
Meteor.methods({
  addScore: function(data) {
    Scores.insert(data,
                  { removeEmptyStrings: false, autoConvert: false },
                  function(error, insertedId) {
      if (error) {
        // TODO: handle the error
        console.log(error);

        return error;
      } else {
        return insertedId;
      }
    });
  },

  findScore: function(data) {
    console.log('accessing DB')
    return Scores.find(data).fetch();
  },

  updateScore: function(data) {
    var selector = {
      route_id: data['route_id'],
      climber_id: data['climber_id']
    };

    data['score_top'] = calculateTop(data[score_string]);
    data['score_bonus'] = calculateBonus(data[score_string]);

    // Probably not needed, see if it throws an error
    // delete data['route_id'];
    // delete data['climber_id'];

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