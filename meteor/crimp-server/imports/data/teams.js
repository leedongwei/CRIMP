import { Mongo } from 'meteor/mongo';
import { ValidatedMethod } from 'meteor/mdg:validated-method';
import { SimpleSchema } from 'meteor/aldeed:simple-schema';

import CRIMP from '../settings';

class TeamsCollection extends Mongo.Collection {
  remove(selector, callback, isRecursive = false) {
    // do nothing yet
  }

  forceRemove(selector, callback) {
    this.remove(selector, callback, true);
  }
}
const Teams = new TeamsCollection('Teams');
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
//Teams.methods.insert =

export default Teams;
