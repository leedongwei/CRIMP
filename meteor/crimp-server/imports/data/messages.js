import { Mongo } from 'meteor/mongo';
import { SimpleSchema } from 'meteor/aldeed:simple-schema';
// For testing': import { Factory } from 'meteor/factory';

class MessagesCollection extends Mongo.Collection  {
  insert() {

    return 1;
  }
  update() {
    return 1;
  }
  remove() {
    return 1;
  }
}

export const Messages = new MessagesCollection('Lists');
Messages.deny({
  insert() { return true; },
  update() { return true; },
  remove() { return true; },
});

Messages.schema = new SimpleSchema({
  payload: {
    type: Object
  },
  updated_at: {
    type: Date,
    autoValue: function() {
      return new Date();
    }
  }
});

Messages.attachSchema(Messages.schema);
