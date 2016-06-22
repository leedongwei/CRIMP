import { Mongo } from 'meteor/mongo';
import { ValidatedMethod } from 'meteor/mdg:validated-method';
import { SimpleSchema } from 'meteor/aldeed:simple-schema';

import CRIMP from '../settings';
import Categories from './categories';

class EventsCollection extends Mongo.Collection {
  remove(selector, callback = null, isRecursive = false) {
    const targetDocs = Events.find(selector);
    if (targetDocs.count() === 0) return 0;

    // Retrieve all affected child Categories
    let childCategory = 0;
    targetDocs.forEach((eventDoc) => {
      childCategory += Categories
                          .find({ 'event._id': eventDoc._id })
                          .count();

      if (isRecursive) {
        childCategory -= Categories.forceRemove({ 'event._id': eventDoc._id });
      }
    });

    // Do not delete Event if there are child Categories
    return (childCategory > 0) ? 0 : super.remove(selector, callback);
  }

  forceRemove(selector) {
    return this.remove(selector, null, true);
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

// if (CRIMP.ENVIRONMENT.NODE_ENV === 'production') {
//   Events.deny({
//     insert() { return true; },
//     update() { return true; },
//     remove() { return true; },
//   });
// }


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
  validate: new SimpleSchema({
    selector: { type: String },
    modifier: { type: String },
    eventDoc: { type: Object },
    'eventDoc.event_name_full': { type: String, optional: true },
    'eventDoc.event_name_short': { type: String, optional: true },
    'eventDoc.time_start': { type: Date, optional: true },
    'eventDoc.time_end': { type: Date, optional: true },
  }).validator(),
  run({ selector, modifier, eventDoc }) {
    return Events.update(selector, { [`${modifier}`]: eventDoc });
  },
});

Events.methods.remove = new ValidatedMethod({
  name: 'Events.method.remove',
  validate: new SimpleSchema({
    selector: { type: String },
    callback: { type: 'function', optional: true },
    isRecursive: { type: Boolean, optional: true },
  }).validator(),
  run({ selector, callback, isRecursive }) {
    return Events.remove({ _id: selector }, callback, isRecursive);
  },
});

Events.methods.forceRemove = new ValidatedMethod({
  name: 'Events.method.forceRemove',
  validate: new SimpleSchema({
    selector: { type: String },
  }).validator(),
  run(selector) {
    return Events.remove({ _id: selector }, null, true);
  },
});

export default Events;
