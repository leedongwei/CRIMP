import { Mongo } from 'meteor/mongo';
import { ValidatedMethod } from 'meteor/mdg:validated-method';
import { SimpleSchema } from 'meteor/aldeed:simple-schema';

import CRIMP from '../settings';
import Scores from './scores';

class ClimbersCollection extends Mongo.Collection {
  remove(selector, callback, isRecursive = false) {
    const targetDocs = Climbers.find(selector);
    if (targetDocs.count() === 0) return 0;

    // Retrieve all affected child Scores
    let childScores = 0;
    targetDocs.forEach((climberDoc) => {
      childScores += Scores
                        .find({ climber_id: climberDoc._id })
                        .count();

      if (isRecursive) {
        if (climberDoc.is_team_category) {
          childScores -= Scores.remove({ climber_id: climberDoc._id });
        }
      }
    });

    // Do not delete Climbers if there are child Scores
    return (childScores > 0)
      ? 0
      : super.remove(selector, callback);
  }
}

const Climbers = new ClimbersCollection('Climbers');
Climbers.schema = new SimpleSchema({
  climber_name: {
    type: String,
  },
  identity: {
    type: String,
    label: 'NRIC or driver license number',
    optional: true,
  },
  affliation: {
    label: 'Affliations of the climber (school, gym etc)',
    type: String,
    optional: true,
  },
  categories: {
    type: [Object],
    label: 'References and data specific to categories that climber is in',
  },
  'categories.$._id': {
    type: String,
  },
  'categories.$.score_tiebreak': {
    type: Number,
    defaultValue: 1,
    label: 'Manually rank climbers who are tied in rank',
  },
  'categories.$.status': {
    type: String,
    label: 'Status icons reflected on scoreboard',
    optional: true,
  },
  'categories.$.additional_status': {
    type: String,
    label: 'Comment to reflect on scoreboard',
    optional: true,
  },
  updated_at: {
    type: Date,
    optional: true,   // optional to pass ValidatedMethod
    autoValue: () => new Date(),
  },
});
Climbers.attachSchema(Climbers.schema);

if (CRIMP.ENVIRONMENT.NODE_ENV === 'production') {
  Climbers.deny({
    insert() { return true; },
    update() { return true; },
    remove() { return true; },
  });
}


Climbers.methods = {};
Climbers.methods.insert = new ValidatedMethod({
  name: 'Climbers.method.insert',
  validate: Climbers.schema.validator(),
  run(climberDoc) {
    return Climbers.insert(climberDoc);
  },
});
Climbers.methods.update = new ValidatedMethod({
  name: 'Climbers.method.update',
  validate: new SimpleSchema({
    selector: { type: String },
    modifier: { type: String },
    climberDoc: { type: Object },
    'climberDoc.climber_name': { type: String, optional: true },
    'climberDoc.identity': { type: String, optional: true },
    'climberDoc.affliation': { type: String, optional: true },
    'climberDoc.categories.$._id': { type: String, optional: true },
    'climberDoc.categories.$.score_tiebreak': { type: String, optional: true },
    'climberDoc.categories.$.status': { type: String, optional: true },
    'climberDoc.categories.$.additional_status': { type: String, optional: true },
  }).validator(),
  run({ selector, modifier, climberDoc }) {
    return Climbers.update(selector, { [`${modifier}`]: climberDoc });
  },
});
Climbers.methods.remove = new ValidatedMethod({
  name: 'Climbers.method.remove',
  validate: new SimpleSchema({
    selector: { type: String },
    callback: { type: 'function', optional: true },
    isRecursive: { type: Boolean, optional: true },
  }).validator(),
  run({ selector, callback, isRecursive }) {
    return Climbers.remove({ _id: selector }, callback, isRecursive);
  },
});
Climbers.methods.forceRemove = new ValidatedMethod({
  name: 'Climbers.method.forceRemove',
  validate: new SimpleSchema({
    selector: { type: String },
  }).validator(),
  run(selector) {
    return Climbers.remove({ _id: selector }, null, true);
  },
});

export default Climbers;
