'use strict';
import { Mongo } from 'meteor/mongo';
import { ValidatedMethod } from 'meteor/mdg:validated-method';
import { SimpleSchema } from 'meteor/aldeed:simple-schema';

// For testing': import { Factory } from 'meteor/factory';

class MessagesCollection extends Mongo.Collection {
  insert(message, callback) {
    return super.insert(message, callback);
  }
  update() {
    return false;
  }
  remove() {
    return false;
  }
}

export const Messages = new MessagesCollection('Messages');
Messages.schema = new SimpleSchema({
  payload: {
    type: Object,
    blackbox: true
  },
  // TODO: Dongwei
  // Expand and validate payload when scoring is decided
  updated_at: {
    type: Date,
    optional: true,   // optional to pass ValidatedMethod
    autoValue: () => {
      return new Date();
    }
  }
});
Messages.attachSchema(Messages.schema);

if (ENVIRONMENT.NODE_ENV === 'production') {
  Messages.deny({
    insert() { return true; },
    update() { return true; },
    remove() { return true; },
  });
}


Messages.methods = {};
Messages.methods.insert = new ValidatedMethod({
  name: 'Messages.method.insert',
  validate: Messages.schema.validator(),
  run(msg) {
    return Messages.insert(msg);
  }
});
