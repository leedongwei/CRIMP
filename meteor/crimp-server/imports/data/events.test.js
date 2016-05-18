/* eslint-env mocha */
/* eslint-disable func-names, prefer-arrow-callback */

import { Meteor } from 'meteor/meteor';
import { Factory } from 'meteor/dburles:factory';
import { Random } from 'meteor/random';
import { faker } from 'meteor/practicalmeteor:faker';
import { chai, assert } from 'meteor/practicalmeteor:chai';
import { resetDatabase } from 'meteor/xolvio:cleaner';

import '../factories';
import Events from './events';


if (Meteor.isServer) {
  describe('Events', function () {

    before(function () {
      // runs before all tests in this block
    });

    after(function () {
      // runs after all tests in this block
    });

    beforeEach(function () {
      // runs before each test in this block
    });

    afterEach(function () {
      // runs after each test in this block
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
        it('valid document', function () {
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

        it('missing fields', function () {
          const newEventId = Events.methods.insert.call({
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

        it('wrong type for date', function () {
          const newEventId = Events.methods.insert.call({
            event_name_short: 'event_name_short',
            time_start: '19 May 2016',
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
      });
    });
  });
}
