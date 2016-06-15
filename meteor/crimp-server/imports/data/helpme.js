import { Mongo } from 'meteor/mongo';
import { ValidatedMethod } from 'meteor/mdg:validated-method';
import { SimpleSchema } from 'meteor/aldeed:simple-schema';

import CRIMP from '../settings';

const HelpMe = new Mongo.Collection('HelpMe');
HelpMe.schema = new SimpleSchema({
  route_id: {
    type: String,
  },
  route_name: {
    type: String,
    label: 'Location that need help',
  },
  category_name: {
    type: String,
    label: 'Category that need help',
  },
  user_id: {
    type: String,
  },
  user_name: {
    type: String,
    label: 'Name of the judge that need help',
  },
  updated_at: {
    type: Date,
    optional: true,   // optional to pass ValidatedMethod
    autoValue: () => new Date(),
  },
});
HelpMe.attachSchema(HelpMe.schema);

if (CRIMP.ENVIRONMENT.NODE_ENV === 'production') {
  HelpMe.deny({
    insert() { return true; },
    update() { return true; },
    remove() { return true; },
  });
}


HelpMe.methods = {};
HelpMe.methods.remove = new ValidatedMethod({
  name: 'HelpMe.methods.remove',
  validate: new SimpleSchema({
    helpmeId: { type: String },
    userId: { type: String },
  }).validator(),
  run({ helpmeId, userId }) {
    CRIMP.checkRoles(CRIMP.roles.admins, userId);
    HelpMe.remove(helpmeId);
  },
});

export default HelpMe;
