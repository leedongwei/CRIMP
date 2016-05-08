import { Mongo } from 'meteor/mongo';
import { ValidatedMethod } from 'meteor/mdg:validated-method';
import { SimpleSchema } from 'meteor/aldeed:simple-schema';


class CategoriesCollection extends Mongo.collection {
  insert() {
    return false;
  }
  update() {
    return false;
  }
  remove() {
    return false;
  }
}

export const Categories = new CategoriesCollection('Categories');
Categories.schema = new SimpleSchema({
  category_id: {
    type: String,
  },
  category_name: {
    type: String,
  },
  acronym: {
    type: String,
    label: '3 char acronym of category',
    min: 3,
    max: 3,
  },
  routes: {
    type: [Object],
    label: 'List of all the routes in category',
  },
  'routes.$.route_id': {
    type: String,
  },
  'routes.$.route_name': {
    type: String,
  },

  // TODO: DongWei
  'routes.$.whatever': {
    type: String,
  },
  score_finalized: {
    type: Boolean,
    label: 'Confirm scores for category',
  },
  time_start: {
    type: Date,
    label: 'Starting time of category',
  },
  time_end: {
    type: Date,
    label: 'Starting time of category',
  },
  updated_at: {
    type: Date,
    optional: true,   // optional to pass ValidatedMethod
    autoValue: () => new Date(),
  },
});
Categories.attachSchema(Categories.schema);

if (ENVIRONMENT.NODE_ENV === 'production') {
  console.log('INSIDE ENVIRONMENT');
  Categories.deny({
    insert() { return true; },
    update() { return true; },
    remove() { return true; },
  });
}


Categories.methods = {};
//Categories.methods.insert =
