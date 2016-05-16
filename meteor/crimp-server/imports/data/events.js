import { Mongo } from 'meteor/mongo';
import { ValidatedMethod } from 'meteor/mdg:validated-method';
import { SimpleSchema } from 'meteor/aldeed:simple-schema';


class EventsCollection extends Mongo.Collection {
  insert() {
    return false;
  }
  update() {
    // TODO: Update denormalized data in Categories
    return false;
  }
  remove() {
    return false;
  }
}

const Events = new EventsCollection('Events');
Events.schema = new SimpleSchema({
  event_name: {
    type: String,
  },
  time_start: {
    type: Date,
    label: 'Starting time of event',
  },
  time_end: {
    type: Date,
    label: 'Starting time of event',
  },
  updated_at: {
    type: Date,
    optional: true,   // optional to pass ValidatedMethod
    autoValue: () => new Date(),
  },
});
Events.attachSchema(Events.schema);

if (CRIMP.ENVIRONMENT.NODE_ENV === 'production') {
  Events.deny({
    insert() { return true; },
    update() { return true; },
    remove() { return true; },
  });
}


Events.methods = {};
//Events.methods.insert =

export default Events;
