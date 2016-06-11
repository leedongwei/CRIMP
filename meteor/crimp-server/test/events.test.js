/*  eslint-env mocha */
/*  eslint-disable
      func-names,
      prefer-arrow-callback,
*/

import { Meteor } from 'meteor/meteor';
import { Factory } from 'meteor/dburles:factory';
import { assert } from 'meteor/practicalmeteor:chai';
import { resetDatabase } from 'meteor/xolvio:cleaner';

import '../imports/factories';
import Events from '../imports/data/events';
import Categories from '../imports/data/categories';

if (!Meteor.isServer) {
  throw new Meteor.Error('not server');
}

function assertAllFields(event) {
  assert.typeOf(event, 'object');
  assert.typeOf(event.event_name_full, 'string');
  assert.typeOf(event.event_name_short, 'string');
  assert.typeOf(event.time_start, 'date');
  assert.typeOf(event.time_end, 'date');
  assert.typeOf(event.updated_at, 'date');
}

describe('Events', function () {
  // TODO: Find a way to stub CategoriesCollection so we don't have to
  // run the custom Categories.remove that involves Teams and Climbers
  beforeEach(function () {
    resetDatabase();
    Factory.create('event');
  });


  describe('Mutator', function () {
    it('builds correctly from factory', function () {
      const targetEvent = Events.findOne({});
      assertAllFields(targetEvent);
    });
  });


  describe('Meteor.methods', function () {
    describe('insert', function () {
      it('insert with valid document', function () {
        const newEventDoc = Factory.tree('event');
        const newEventId = Events.methods.insert.call(newEventDoc);

        const newEvent = Events.findOne(newEventId);
        assertAllFields(newEvent);
      });

      it('reject extra fields', function () {
        const eventDoc = Factory.tree('event');
        eventDoc.extra_field = true;

        assert.throws(() => {
          Events.methods.insert.call(eventDoc);
        }, Meteor.Error);
      });

      it('reject missing fields', function () {
        const eventDoc = Factory.tree('event');
        delete eventDoc.time_end;

        assert.throws(() => {
          Events.methods.insert.call(eventDoc);
        }, Meteor.Error);
      });

      it('reject wrong types', function () {
        let eventDoc;

        // string on time_start
        eventDoc = Factory.tree('event');
        eventDoc.time_start = '20 May 2016';
        assert.throws(() => {
          Events.methods.insert.call(eventDoc);
        }, Meteor.Error);

        // number on time_start
        eventDoc = Factory.tree('event');
        eventDoc.time_start = 123456;
        assert.throws(() => {
          Events.methods.insert.call(eventDoc);
        }, Meteor.Error);
      });
    });

    describe('update', function () {
      it('update with valid document', function () {
        const targetEvent = Events.findOne({});
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
        const targetEvent = Events.findOne({});
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
        }, Meteor.Error);

        // Try boolean in string
        assert.throws(() => {
          Events.methods.update.call({
            selector: targetEvent._id,
            modifier: '$set',
            eventDoc: { event_name_full: boolean },
          });
        }, Meteor.Error);

        // Try object in string
        assert.throws(() => {
          Events.methods.update.call({
            selector: targetEvent._id,
            modifier: '$set',
            eventDoc: { event_name_full: object },
          });
        }, Meteor.Error);

        // Try number in date
        assert.throws(() => {
          Events.methods.update.call({
            selector: targetEvent._id,
            modifier: '$set',
            eventDoc: { time_start: number },
          });
        }, Meteor.Error);
      });
    });

    describe('remove', function () {
      it('delete with _id, no children', function () {
        const targetEvent = Events.findOne({});
        const removedEvents = Events.methods.remove.call({
          selector: targetEvent._id,
        });

        assert.equal(removedEvents, 1);
        assert.isUndefined(Events.findOne(targetEvent._id));
      });

      it('reject event with child because isRecursive is false', function () {
        const targetEvent = Events.findOne({});
        const newCategoryDoc = Factory.build('category');
        newCategoryDoc.event = targetEvent;
        Categories.insert(newCategoryDoc);

        const removedEvents = Events.methods.remove.call({
          selector: targetEvent._id,
        });

        assert.equal(removedEvents, 0);
        assert.isDefined(Events.findOne(targetEvent._id));
      });

      it('delete event with child because isRecursive is true', function () {
        const targetEvent = Events.findOne({});

        // Set 2 child Categories under Event, and 1 orphan Category
        let newCategoryDoc;
        newCategoryDoc = Factory.build('category');
        newCategoryDoc.event = targetEvent;
        Categories.insert(newCategoryDoc);
        newCategoryDoc = Factory.build('category-ifsc');
        newCategoryDoc.event = targetEvent;
        Categories.insert(newCategoryDoc);
        Factory.create('category-dummy');

        // Ensure Categories are inserted correctly
        assert.equal(Categories.find({}).count(), 3);

        const removedEvents = Events.methods.remove.call({
          selector: targetEvent._id,
          isRecursive: true,
        });

        assert.equal(removedEvents, 1);
        assert.equal(Categories.find({}).count(), 1);
        assert.isUndefined(Events.findOne(targetEvent._id));
      });

      it('reject non ._id selectors', function () {
        const targetEvent = Events.findOne({});

        assert.throws(() => {
          Events.methods.remove.call({
            selector: {
              event_name_full: targetEvent.event_name_full,
            },
          });
        }, Meteor.Error);
      });
    });
  });
});
