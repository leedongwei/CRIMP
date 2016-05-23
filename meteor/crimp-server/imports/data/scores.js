import { Mongo } from 'meteor/mongo';
import { ValidatedMethod } from 'meteor/mdg:validated-method';
import { SimpleSchema } from 'meteor/aldeed:simple-schema';

import CRIMP from '../settings';

const Scores = new Mongo.Collection('Scores');
Scores.schema = new SimpleSchema({
  category_id: {
    type: String,
    label: 'Reference to parent category',
  },
  climber_id: {
    type: String,
    label: 'Reference to parent climber',
  },
  marker_id: {
    type: String,
    label: 'Category-specific ID given to a climber',
    // unique within a category, but not unique in the entire system
    // enforced by
  },
  scores: {
    type: [Object],
  },
  'scores.$.route_id': {
    type: String,
  },
  'scores.$.score_string': {
    type: String,
    autoValue: function defaultScoreString() {
      return this.isSet ? this.value : '';
    },
  },
  updated_at: {
    type: Date,
    optional: true,   // optional to pass ValidatedMethod
    autoValue: () => new Date(),
  },
});
Scores.attachSchema(Scores.schema);

if (CRIMP.ENVIRONMENT.NODE_ENV === 'production') {
  Scores.deny({
    insert() { return true; },
    update() { return true; },
    remove() { return true; },
  });
}


Scores.methods = {};
Scores.methods.update = new ValidatedMethod({
  name: 'Scores.method.update',
  validate: new SimpleSchema({
    selector: { type: String },
    modifier: { type: String },
    scoreDoc: { type: Object },
    'scoreDoc.category_name': { type: Object },
    'scoreDoc.score_string': { type: String },
  }).validator(),
  run({ selector, modifier, categoryDoc }) {
    return Scores.update(selector, { [`${modifier}`]: categoryDoc });
  },
});


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Utility functions                                      *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
Scores.methods.isMarkerIdUnique = new ValidatedMethod({
  name: 'Scores.method.isMarkerIdUnique',
  validate: new SimpleSchema({
    marker_id: { type: String },
    category_id: { type: String },
  }).validator(),
  run({ marker_id, category_id }) {
    return Scores.find({ marker_id, category_id }).count() === 0;
  },
});
Scores.methods.isClimberUnique = new ValidatedMethod({
  name: 'Scores.method.isClimberUnique',
  validate: new SimpleSchema({
    climber_id: { type: String },
    category_id: { type: String },
  }).validator(),
  run({ climber_id, category_id }) {
    return Scores.find({ climber_id, category_id }).count() === 0;
  },
});

export default Scores;
