import { Mongo } from 'meteor/mongo';
import { ValidatedMethod } from 'meteor/mdg:validated-method';
import { SimpleSchema } from 'meteor/aldeed:simple-schema';
import { scoreSystemsNames } from '../scoreSystem.js';


class CategoriesCollection extends Mongo.Collection {
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
  score_system: {
    type: String,
    label: 'Name of score system used in category',
    allowedValues: scoreSystemsNames,
  },
  score_finalized: {
    type: Boolean,
    label: 'Confirm scores for category',
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
  'routes.$.score_rules': {
    type: Object,
    label: 'Score rules of a route',
    blackbox: true,
  },
  time_start: {
    type: Date,
    label: 'Starting time of category',
  },
  time_end: {
    type: Date,
    label: 'Starting time of category',
  },
  event: {
    type: Object,
    label: 'Denormalized data of parent event',
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
