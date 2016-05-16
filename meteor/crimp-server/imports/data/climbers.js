import { Mongo } from 'meteor/mongo';
import { ValidatedMethod } from 'meteor/mdg:validated-method';
import { SimpleSchema } from 'meteor/aldeed:simple-schema';


class ClimbersCollection extends Mongo.Collection {
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

const Climbers = new ClimbersCollection('Climbers');
Climbers.schema = new SimpleSchema({
  climber_name: {
    type: String,
  },
  identity: {
    type: String,
    label: 'NRIC or driver license number',
    optional: true
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
//Climbers.methods.insert =

export default Climbers;
