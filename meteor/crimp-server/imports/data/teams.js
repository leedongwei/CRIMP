import { Mongo } from 'meteor/mongo';
import { ValidatedMethod } from 'meteor/mdg:validated-method';
import { SimpleSchema } from 'meteor/aldeed:simple-schema';

import CRIMP from '../settings';

const Teams = new Mongo.Collection('Teams');
Teams.schema = new SimpleSchema({
  team_name: {
    type: String,
  },
  category_id: {
    type: String,
    label: 'Reference to parent category',
  },
  climbers: {
    type: [String],
    label: 'References to climbers in team',
  },
  updated_at: {
    type: Date,
    optional: true,   // optional to pass ValidatedMethod
    autoValue: () => new Date(),
  },
});
Teams.attachSchema(Teams.schema);

if (CRIMP.ENVIRONMENT.NODE_ENV === 'production') {
  Teams.deny({
    insert() { return true; },
    update() { return true; },
    remove() { return true; },
  });
}


Teams.methods = {};
Teams.methods.insert = new ValidatedMethod({
  name: 'Teams.method.insert',
  validate: Teams.schema.validator(),
  run(teamDoc) {
    return Teams.insert(teamDoc);
  },
});
Teams.methods.update = new ValidatedMethod({
  name: 'Teams.method.update',
  validate: new SimpleSchema({
    selector: { type: String },
    modifier: { type: String },
    teamDoc: { type: Object },
    'teamDoc.team_name': { type: String, optional: true },
    'teamDoc.category_id': { type: String, optional: true },
    'teamDoc.climbers': { type: [String], optional: true },
  }).validator(),
  run({ selector, modifier, teamDoc }) {
    return Teams.update(selector, { [`${modifier}`]: teamDoc });
  },
});
Teams.methods.remove = new ValidatedMethod({
  name: 'Teams.method.remove',
  validate: new SimpleSchema({
    selector: { type: String },
    callback: { type: 'function', optional: true },
  }).validator(),
  run({ selector, callback }) {
    return Teams.remove({ _id: selector }, callback);
  },
});

export default Teams;
