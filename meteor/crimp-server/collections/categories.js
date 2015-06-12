Categories = new Mongo.Collection('categories');
Schema.Category = new SimpleSchema({
  name: {
    label: 'Name of category',
    type: String,
  },
  id: {
    label: 'Acronym for name',
    type: String,
    index: true,
    unique: true,
    min: 3,
    max: 3
  },
  route_count: {
    label: 'Number of routes',
    type: Number,
    min: 1
  },
  scores_finalized: {
    label: 'Check if chief judge validates scores',
    type: Boolean,
    autoValue: function() {
      return false;
    }
  },
  time_start: {
    label: 'Starting date and time',
    type: Date,
    optional: true
  },
  time_end: {
    label: 'Ending date and time',
    type: Date,
    optional: true
  }
});


Categories.attachSchema(Schema.Category);

// TODO: Ensure admin-only access
// Use audit-argument-checks for checks?
Meteor.methods({
  addCategory: function(data) {
    Categories.insert(data, function(error, insertedId) {
      if (error)  throw error;
    });
  },

  findCategory: function(data) {
    return Categories.find(data).fetch();
  },

  updateCategory: function(data) {

    Categories.update(data.selector, { $set: data.modifier },
                      function(error, updatedCount) {
      if (error)  throw error;
    });
  },

  deleteCategory: function(data) {
    Categories.remove(data, function(error, removedCount) {
      if (error)  throw error;
    });
  }
});