/*  eslint-env mocha */
/*  eslint-disable
      func-names,
      prefer-arrow-callback,
*/

import { Meteor } from 'meteor/meteor';
import { Mongo } from 'meteor/mongo';
import { Factory } from 'meteor/dburles:factory';
import { faker } from 'meteor/practicalmeteor:faker';
import { chai, assert, expect } from 'meteor/practicalmeteor:chai';
import { resetDatabase } from 'meteor/xolvio:cleaner';

import '../imports/factories';
import Events from '../imports/data/events';
import Categories from '../imports/data/categories';

if (!Meteor.isServer) {
  throw new Meteor.Error('not server');
}

describe('Events', function () {
  /**
   *  TODO: Find a way to stub CategoriesCollection so we don't have to
   *  run the custom Categories.remove that involves Teams and Climbers
   */
  beforeEach(function () {
    resetDatabase();
  });


  describe('Mutator', function () {
    it('builds from factory', function () {
      const newEvent = Factory.create('event');

      assert.typeOf(newEvent, 'object');
      assert.typeOf(newEvent.event_name_full, 'string');
      assert.typeOf(newEvent.event_name_short, 'string');
      assert.typeOf(newEvent.time_start, 'date');
      assert.typeOf(newEvent.time_end, 'date');
      assert.typeOf(newEvent.updated_at, 'date');
    });
  });


  describe('Meteor.methods', function () {
    describe('insert', function () {
      it('insert with valid document', function () {
        const newEventId = Events.methods.insert.call({
          event_name_full: faker.company.companyName(),
          event_name_short: 'event_name_short',
          time_start: new Date(),
          time_end: new Date(),
        });

        const newEvent = Events.findOne(newEventId);
        assert.typeOf(newEvent, 'object');
        assert.typeOf(newEvent.event_name_full, 'string');
        assert.typeOf(newEvent.event_name_short, 'string');
        assert.typeOf(newEvent.time_start, 'date');
        assert.typeOf(newEvent.time_end, 'date');
        assert.typeOf(newEvent.updated_at, 'date');
      });

      it('reject extra fields', function () {
        assert.throws(() => {
          Events.methods.insert.call({
            event_name_full: faker.company.companyName(),
            event_name_short: 'event_name_short',
            time_start: new Date(),
            time_end: new Date(),
            extra_field: true,
          });
        }, Meteor.Error);
      });

      it('reject missing fields', function () {
        assert.throws(() => {
          Events.methods.insert.call({
            event_name_full: faker.company.companyName(),
            event_name_short: 'event_name_short',
            time_start: new Date(),
          });
        }, Meteor.Error);
      });

      it('reject wrong types', function () {
        assert.throws(() => {
          Events.methods.insert.call({
            event_name_full: faker.company.companyName(),
            event_name_short: 'event_name_short',
            time_start: '19 May 2016',
            time_end: new Date(),
          });
        }, Meteor.Error);

        assert.throws(() => {
          Events.methods.insert.call({
            event_name_full: faker.company.companyName(),
            event_name_short: 123456,
            time_start: new Date(),
            time_end: new Date(),
          });
        }, Meteor.Error);
      });
    });

    describe('update', function () {
      it('update with valid document', function () {
        const targetEvent = Factory.create('event');
        const newEventName = 'Updated Event Name';

        Events.methods.update.call({
          selector: targetEvent._id,
          modifier: '$set',
          eventDoc: { event_name_full: newEventName },
        });

        assert.equal(
          Events.findOne(targetEvent._id).event_name_full,
          newEventName
        );
      });

      it('reject wrong types', function () {
        const targetEvent = Factory.create('event');
        const number = 123456;
        const boolean = true;
        const object = {};

        // Try number in string
        assert.throws(() => {
          Events.methods.update.call({
            selector: targetEvent._id,
            modifier: '$set',
            eventDoc: { event_name_full: number },
          });
        }, Error);

        // Try boolean in string
        assert.throws(() => {
          Events.methods.update.call({
            selector: targetEvent._id,
            modifier: '$set',
            eventDoc: { event_name_full: boolean },
          });
        }, Error);

        // Try object in string
        assert.throws(() => {
          Events.methods.update.call({
            selector: targetEvent._id,
            modifier: '$set',
            eventDoc: { event_name_full: object },
          });
        }, Error);

        // Try number in date
        assert.throws(() => {
          Events.methods.update.call({
            selector: targetEvent._id,
            modifier: '$set',
            eventDoc: { time_start: number },
          });
        }, Error);
      });
    });

    describe('remove', function () {
      it('delete with _id, no children', function () {
        const newEvent = Factory.create('event');
        const removedEvents = Events.methods.remove.call({
          selector: newEvent._id,
        });

        assert.equal(removedEvents, 1);
        assert.isUndefined(Events.findOne(newEvent._id));
      });

      it('reject event with child because isRecursive is false', function () {
        const newEvent = Factory.create('event');
        const newCategoryDoc = Factory.build('category');
        newCategoryDoc.event = newEvent;
        Categories.insert(newCategoryDoc);

        const removedEvents = Events.methods.remove.call({
          selector: newEvent._id,
        });

        assert.equal(removedEvents, 0);
        assert.isDefined(Events.findOne(newEvent._id));
      });

      /**
       *  Need CategoriesCollection stub to isolate Events
       */
      it('delete event with child because isRecursive is true', function () {
        const newEvent = Factory.create('event');

        // Set 3 child Categories under Event
        let newCategoryDoc;
        newCategoryDoc = Factory.build('category');
        newCategoryDoc.event = newEvent;
        Categories.insert(newCategoryDoc);
        newCategoryDoc = Factory.build('category');
        newCategoryDoc.event = newEvent;
        Categories.insert(newCategoryDoc);
        newCategoryDoc = Factory.build('category');
        newCategoryDoc.event = newEvent;
        Categories.insert(newCategoryDoc);

        const removedEvents = Events.methods.remove.call({
          selector: newEvent._id,
          isRecursive: true,
        });

        assert.equal(removedEvents, 1);
        assert.equal(Categories.find({}).count(), 0);
        assert.isUndefined(Events.findOne(newEvent._id));
      });

      it('reject non _.id selectors', function () {
        const newEvent = Factory.create('event');

        assert.throws(() => {
          Events.methods.remove.call({
            selector: {
              event_name_full: newEvent.event_name_full,
            },
          });
        }, Error);
      });
    });
  });
});
