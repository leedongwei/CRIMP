Scores = new Mongo.Collection('score');
Schema.Score = new SimpleSchema({
  climber_id: {
    label: "ID of climber",
    type: String,
    min: 6,
    max: 6
  },
  admin_id: {
    label: "ID of admin",
    type: String,
    min: 6,
    max: 6
  },
  category_acronym: {
    type: Number,
    label: "Acronym of category",
    min: 1
  },
  climbers: {
    label: "References to climber documents",
    // TODO: Change to Climber_Schema or array of sorts
    type: String
  },
  time_start: {
    label: "Starting date and time",
    type: Date,
    optional: true
  },
  time_end: {
    label: "Ending date and time",
    type: Date,
    optional: true
  }
});


Scores.attachSchema(Schema.Score);

// TODO: Ensure admin-only access
Meteor.methods({

});