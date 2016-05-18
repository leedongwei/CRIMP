import { Mongo } from 'meteor/mongo';
import { ValidatedMethod } from 'meteor/mdg:validated-method';
import { SimpleSchema } from 'meteor/aldeed:simple-schema';

import CRIMP from '../settings';
import Categories from './categories';

class EventsCollection extends Mongo.Collection {
  remove(selector, callback, isRecursive = false) {
    const targetDocs = Events.find(selector);
    if (targetDocs.count() === 0) return 0;

    // Retrieve all affected child Categories
    let childCategory = 0;
    targetDocs.forEach((eventDoc) => {
      if (isRecursive) {
        Categories.forceRemove({ 'event._id': eventDoc._id });
      } else {
        childCategory += Categories
                          .find({ 'event._id': eventDoc._id })
                          .count();
      }
    });

    // Do not delete Event if there are child Categories
    return (childCategory > 0) ? 0 : super.remove(selector, callback);
  }

  forceRemove(selector, callback) {
    this.remove(selector, callback, true);
  }
}

const Events = new EventsCollection('Events');
Events.schema = new SimpleSchema({
  event_name_full: {
    type: String,
    max: 60,
  },
  event_name_short: {
    type: String,
    max: 20,
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
Events.methods.insert = new ValidatedMethod({
  name: 'Events.method.insert',
  validate: Events.schema.validator(),
  run(eventDoc) {
    return Events.insert(eventDoc);
  },
});
Events.methods.update = new ValidatedMethod({
  name: 'Events.method.update',
  validate: Events.schema.validator(),
  run(selector, eventDoc) {
    return Events.update(eventDoc);
  },
});
Events.methods.remove = new ValidatedMethod({
  name: 'Events.method.remove',
  validate: () => {},
  run(selector) {
    return Events.remove(selector);
  },
});
Events.methods.forceRemove = new ValidatedMethod({
  name: 'Events.method.forceRemove',
  validate: () => {},
  run(selector) {
    return Events.forceRemove(selector);
  },
});

export default Events;
