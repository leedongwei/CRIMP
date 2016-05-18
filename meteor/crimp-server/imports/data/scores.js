import { Mongo } from 'meteor/mongo';
import { ValidatedMethod } from 'meteor/mdg:validated-method';
import { SimpleSchema } from 'meteor/aldeed:simple-schema';

import CRIMP from '../settings';

class ScoresCollection extends Mongo.Collection {
  // insert() {
  //   return false;
  // }
  // update() {
  //   return false;
  // }
  // remove() {
  //   return false;
  // }
}

const Scores = new ScoresCollection('Scores');
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
  },
  scores: {
    type: [Object],
  },
  'scores.$.route_id': {
    type: String,
  },
  'scores.$.score_string': {
    type: String,
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

export default Scores;
