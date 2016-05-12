import { Mongo } from 'meteor/mongo';
import { ValidatedMethod } from 'meteor/mdg:validated-method';
import { SimpleSchema } from 'meteor/aldeed:simple-schema';
import { scoreSystemsNames } from '../scoreSystem.js';


class CategoriesCollection extends Mongo.Collection {
  // no special functions needed right now
}

/**
 *  Note: Categories contains denormalized data from Events
 */
const Categories = new CategoriesCollection('Categories');
Categories.schema = new SimpleSchema({
  category_name: {
    type: String,
  },
  acronym: {
    type: String,
    label: '3 char acronym of category',
    min: 3,
    max: 3,
  },
  is_team_category: {
    type: Boolean,
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
  score_system: {
    type: String,
    label: 'Name of score system used in category',
    allowedValues: scoreSystemsNames,
  },

  /**
   * Embedded data for the routes in a category
   */
  routes: {
    type: [Object],
    label: 'List of all the routes in category',
  },
  'routes.$._id': {
    type: String,
  },
  'routes.$.route_name': {
    type: String,
  },
  // TODO: DongWei
  'routes.$.score_rules': {
    type: Object,
    label: 'Score rules specific to a route',
    optional: true,
    blackbox: true,
  },

  /**
   * Denormalized data of parent event
   */
  event: {
    type: Object,
  },
  'event._id': {
    type: String,
  },
  'event.event_name': {
    type: String,
  },

  updated_at: {
    type: Date,
    optional: true,   // optional to pass ValidatedMethod
    autoValue: () => new Date(),
  },
});
Categories.attachSchema(Categories.schema);

if (ENVIRONMENT.NODE_ENV === 'production') {
  // TODO: Remove console.log
  console.log('Categories: inside env.nod_env');
  Categories.deny({
    insert() { return true; },
    update() { return true; },
    remove() { return true; },
  });
}


Categories.methods = {};
//Categories.methods.insert =

export default Categories;
