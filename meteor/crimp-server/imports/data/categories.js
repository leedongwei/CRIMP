import { Mongo } from 'meteor/mongo';
import { ValidatedMethod } from 'meteor/mdg:validated-method';
import { SimpleSchema } from 'meteor/aldeed:simple-schema';
import { scoreSystemsNames } from '../scoreSystem.js';

import CRIMP from '../settings';
import Teams from './teams';
import Climbers from './climbers';

class CategoriesCollection extends Mongo.Collection {
  remove(selector, callback, isRecursive = false) {
    const targetDocs = Categories.find(selector);
    if (targetDocs.count() === 0) return 0;

    // Retrieve all affected child Teams and Climbers
    let childTeams = 0;
    let childClimbers = 0;
    targetDocs.forEach((categoryDoc) => {
      if (isRecursive) {
        Teams.forceRemove({ category_id: categoryDoc._id });
        Climbers.methods.removeFromCategory({
          category_id: categoryDoc._id,
        });
      } else {
        childTeams += Teams
                        .find({ category_id: categoryDoc._id })
                          .count();
        childClimbers += Climbers
                            .find({ 'categories.$._id': categoryDoc._id })
                            .count();
      }
    });

    // Do not delete Event if there are child Categories
    return (childTeams + childClimbers > 0)
      ? 0
      : super.remove(selector, callback);
  }

  forceRemove(selector, callback) {
    this.remove(selector, callback, true);
  }
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
    blackbox: true,
  },
  'event._id': {
    type: String,
  },
  'event.event_name_full': {
    type: String,
  },

  updated_at: {
    type: Date,
    optional: true,   // optional to pass ValidatedMethod
    autoValue: () => new Date(),
  },
});
Categories.attachSchema(Categories.schema);

if (CRIMP.ENVIRONMENT.NODE_ENV === 'production') {
  Categories.deny({
    insert() { return true; },
    update() { return true; },
    remove() { return true; },
  });
}


Categories.methods = {};
Categories.methods.insert = new ValidatedMethod({
  name: 'Categories.method.insert',
  validate: Categories.schema.validator(),
  run(parentEventDoc, categoryDoc) {
    const newDoc = categoryDoc;
    newDoc.event = {
      _id: parentEventDoc._id,
      event_name_full: parentEventDoc.event_name_full,
    };

    return Categories.insert(newDoc);
  },
});
Categories.methods.update = new ValidatedMethod({
  name: 'Categories.method.update',
  validate: Categories.schema.validator(),
  run(selector, categoryDoc) {
    return Categories.update(categoryDoc);
  },
});
Categories.methods.remove = new ValidatedMethod({
  name: 'Categories.method.remove',
  validate: () => {},
  run(selector) {
    return Categories.remove(selector);
  },
});
Categories.methods.forceRemove = new ValidatedMethod({
  name: 'Categories.method.forceRemove',
  validate: () => {},
  run(selector) {
    return Categories.forceRemove(selector);
  },
});

export default Categories;
