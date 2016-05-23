import { Mongo } from 'meteor/mongo';
import { ValidatedMethod } from 'meteor/mdg:validated-method';
import { SimpleSchema } from 'meteor/aldeed:simple-schema';
import { scoreSystemsNames } from '../scoreSystem.js';

import CRIMP from '../settings';
import Teams from './teams';
import Scores from './scores';

class CategoriesCollection extends Mongo.Collection {
  remove(selector, callback, isRecursive = false) {
    const targetDocs = Categories.find(selector);
    if (targetDocs.count() === 0) return 0;

    // Retrieve all affected child Teams and Scores
    let childTeams = 0;
    let childScores = 0;
    targetDocs.forEach((categoryDoc) => {
      if (isRecursive) {
        childScores -= Scores.remove({ category_id: categoryDoc._id });
        childTeams -= categoryDoc.is_team_category
                        ? Teams.remove({ category_id: categoryDoc._id })
                        : 0;
      } else {
        childScores += Scores.find({ category_id: categoryDoc._id }).count();
        childTeams += categoryDoc.is_team_category
                        ? Teams.find({ category_id: categoryDoc._id }).count()
                        : 0;
      }
    });

    // Do not delete Categories if there are child Teams/Scores
    return (childTeams + childScores > 0)
      ? 0
      : super.remove(selector, callback);
  }

  forceRemove(selector) {
    return this.remove(selector, null, true);
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
  is_score_finalized: {
    type: Boolean,
    label: 'Confirm scores for category',
  },
  climber_count: {
    type: Number,
    label: 'Count climbers added to category (will not decrease)',
    // If you holding a sick event and you need more than 999 Climbers
    // in a Category, you can change the count.length to 4 at
    // 'Climbers.methods.addToCategory' inside 'import/data/climber.js'
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
  'routes.$.score_rules': {
    type: Object,
    label: 'Score rules specific to a route',
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
  validate: new SimpleSchema({
    parentEvent: { type: Object, blackbox: true },
    'parentEvent._id': { type: String },
    'parentEvent.event_name_full': { type: String },
    categoryDoc: { type: Categories.schema },
  }).validator(),
  run({ parentEvent, categoryDoc }) {
    const newDoc = categoryDoc;
    newDoc.event = {
      _id: parentEvent._id,
      event_name_full: parentEvent.event_name_full,
    };

    return Categories.insert(newDoc);
  },
});

Categories.methods.update = new ValidatedMethod({
  name: 'Categories.method.update',
  validate: new SimpleSchema({
    selector: { type: String },
    modifier: { type: String },
    categoryDoc: { type: Object },
    'categoryDoc.category_name': { type: String, optional: true },
    'categoryDoc.acronym': { type: String, optional: true },
    'categoryDoc.is_team_category': { type: Boolean, optional: true },
    'categoryDoc.is_score_finalized': { type: Boolean, optional: true },
    'categoryDoc.time_Start': { type: Date, optional: true },
    'categoryDoc.time_end': { type: Date, optional: true },
    'categoryDoc.score_system': { type: String, optional: true },
    'categoryDoc.routes': { type: Object, optional: true },
    'categoryDoc.routes.$._id': { type: String, optional: true },
    'categoryDoc.routes.$.route_name': { type: String, optional: true },
    'categoryDoc.routes.$.score_rules': { type: Object,
                                          optional: true,
                                          blackbox: true },
  }).validator(),
  run({ selector, modifier, categoryDoc }) {
    /**
     *  Updating of parent Event is not allowed
     */
    return Categories.update(selector, { [`${modifier}`]: categoryDoc });
  },
});

Categories.methods.remove = new ValidatedMethod({
  name: 'Categories.method.remove',
  validate: new SimpleSchema({
    selector: { type: String },
    callback: { type: 'function', optional: true },
    isRecursive: { type: Boolean, optional: true },
  }).validator(),
  run({ selector, callback, isRecursive }) {
    return Categories.remove({ _id: selector }, callback, isRecursive);
  },
});

Categories.methods.forceRemove = new ValidatedMethod({
  name: 'Categories.method.forceRemove',
  validate: new SimpleSchema({
    selector: { type: String },
  }).validator(),
  run(selector) {
    return Categories.remove({ _id: selector }, null, true);
  },
});


export default Categories;
