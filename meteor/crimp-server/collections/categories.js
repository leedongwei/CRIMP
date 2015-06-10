Categories = new Mongo.Collection('categories');
Schema.Category = new SimpleSchema({
  name: {
    label: "Name of category",
    type: String,
  },
  acronym: {
    label: "Acronym for name",
    type: String,
    index: true,
    unique: true,
    min: 3,
    max: 3
  },
  route_count: {
    label: "Number of routes",
    type: Number,
    min: 1
  },
  climbers: {
    // TODO: Change to Climber_Schema or array of sorts
    label: "References to climber documents",
    type: String
  },
  scores_finalized: {
    label: "Check if chief judge validates scores",
    type: Boolean
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


Categories.attachSchema(Schema.Category);

// TODO: Ensure admin-only access
// Use audit-argument-checks for checks?
Meteor.methods({
  createCategory: function(data) {
    Categories.insert(data, function(error, insertedId) {
      if (error) {
        // TODO: handle the error
        console.log(error);

        return error;
      } else {
        return insertedId;
      }
    });
  },

  findCategory: function(data) {
    return Categories.find(data).fetch();
  },

  updateCategory: function(data) {

    Categories.update(selector, modifier, function(error, updatedCount) {
      if (error) {
        // TODO: handle the error
        console.log(error);

        return error;
      } else {
        return updatedCount;
      }
    });
  },

  deleteCategory: function(data) {
    Categories.remove(data, function(error, removedCount) {
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