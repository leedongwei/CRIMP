Categories = new Mongo.Collection('categories');
CRIMP.schema.category = new SimpleSchema({
  category_name: {
    label: 'Name of category',
    type: String,
  },
  category_id: {
    label: 'Acronym for name',
    type: String,
    index: true,
    unique: true,
    min: 3,
    max: 3,
    denyUpdate: true
  },
  route_count: {
    label: 'Number of routes',
    type: Number,
    min: 1,
    denyUpdate: true
  },
  scores_finalized: {
    label: 'Check if scores are finalized by Chief Judge',
    type: Boolean,
    autoValue() {
      if (this.isInsert) {
        return false;
      }
    }
  },
  time_start: {
    label: 'Starting date and time (Optional)',
    type: Date,
    optional: true
  },
  time_end: {
    label: 'Ending date and time (Optional)',
    type: Date,
    optional: true
  }
});


Categories.attachSchema(CRIMP.schema.category);


/**
 *  I/O operations for category do not need special methods.
 *  TODO: Delete after confirming
 */
// Meteor.methods({
//   addCategory(categoryDoc) {
//   },

//   updateCategory(data) {
//   },

//   deleteCategory(data) {
//   }

// });