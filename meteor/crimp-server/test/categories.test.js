/*  eslint-env mocha */
/*  eslint-disable
      func-names,
      prefer-arrow-callback,
*/

import { Meteor } from 'meteor/meteor';
import { Factory } from 'meteor/dburles:factory';
import { faker } from 'meteor/practicalmeteor:faker';
import { assert } from 'meteor/practicalmeteor:chai';
import { resetDatabase } from 'meteor/xolvio:cleaner';

import '../imports/factories';
import Events from '../imports/data/events';
import Categories from '../imports/data/categories';
import Teams from '../imports/data/teams';
import Climbers from '../imports/data/climbers';
import Scores from '../imports/data/scores';

if (!Meteor.isServer) {
  throw new Meteor.Error('not server');
}

function assertAllFields(category) {
  assert.typeOf(category, 'object');
  assert.typeOf(category._id, 'string');
  assert.typeOf(category.category_name, 'string');
  assert.typeOf(category.acronym, 'string');
  assert.typeOf(category.is_team_category, 'boolean');
  assert.typeOf(category.is_score_finalized, 'boolean');
  assert.typeOf(category.time_start, 'date');
  assert.typeOf(category.time_end, 'date');
  assert.typeOf(category.score_system, 'string');
  assert.typeOf(category.routes, 'array');
  assert.typeOf(category.routes[0]._id, 'string');
  assert.typeOf(category.routes[0].route_name, 'string');
  assert.typeOf(category.event, 'object');
  assert.typeOf(category.event._id, 'string');
  assert.typeOf(category.event.event_name_full, 'string');
}

describe('Categories', function () {
  beforeEach(function () {
    resetDatabase();

    // Creates a Category document with a parent Event
    const newEvent = Factory.create('event');
    const newCategoryDoc = Factory.build('category');
    newCategoryDoc.event = newEvent;
    Categories.insert(newCategoryDoc);
  });


  describe('Mutator', function () {
    it('builds correctly from factory', function () {
      const targetCategory = Categories.findOne({});
      assertAllFields(targetCategory);
    });
  });


  describe('Meteor.methods', function () {
    describe('insert', function () {
      it('insert with valid document', function () {
        const parentEventDoc = Events.findOne({});
        const categoryDoc = Factory.tree('category');

        const newCategoryId = Categories.methods.insert.call({
          parentEventDoc,
          categoryDoc,
        });

        const newCategory = Categories.findOne(newCategoryId);
        assertAllFields(newCategory);
      });

      it('reject extra fields', function () {
        const parentEventDoc = Events.findOne({});
        const categoryDoc = Factory.tree('category');
        categoryDoc.extra_field = true;

        assert.throws(() => {
          Categories.methods.insert.call({
            parentEventDoc,
            categoryDoc,
          });
        }, Meteor.Error);
      });

      it('reject missing fields', function () {
        const parentEventDoc = Events.findOne({});
        const categoryDoc = Factory.tree('category');
        delete categoryDoc.score_system;

        assert.throws(() => {
          Categories.methods.insert.call({
            parentEventDoc,
            categoryDoc,
          });
        }, Meteor.Error);
      });

      it('reject wrong types', function () {
        const parentEventDoc = Events.findOne({});
        const categoryDoc = Factory.tree('category');
        categoryDoc.is_score_finalized = 1;

        assert.throws(() => {
          Categories.methods.insert.call({
            parentEventDoc,
            categoryDoc,
          });
        }, Meteor.Error);
      });
    });

    describe('update', function () {
      it('update with valid document', function () {
        const targetCategory = Categories.findOne({});
        const newCategoryName = 'Updated Category Name';

        Categories.methods.update.call({
          selector: targetCategory._id,
          modifier: '$set',
          categoryDoc: { category_name: newCategoryName },
        });

        assert.equal(
          Categories.findOne(targetCategory._id).category_name,
          newCategoryName
        );
      });

      it('reject wrong types', function () {
        const targetCategory = Categories.findOne({});
        const number = 123456;
        const boolean = true;

        // Try number in boolean
        assert.throws(() => {
          Categories.methods.update.call({
            selector: targetCategory._id,
            modifier: '$set',
            eventDoc: { is_team_category: number },
          });
        }, Meteor.Error);

        // Try string in boolean
        assert.throws(() => {
          Categories.methods.update.call({
            selector: targetCategory._id,
            modifier: '$set',
            eventDoc: { is_score_finalized: boolean },
          });
        }, Meteor.Error);
      });

      it('reject change of parent event', function () {
        const targetCategory = Categories.findOne({});
        const fakeEvent = { _id: 'fake', event_name_full: 'fake' };

        assert.throws(() => {
          Categories.methods.update.call({
            selector: targetCategory._id,
            modifier: '$set',
            categoryDoc: { event: fakeEvent },
          });
        }, Meteor.Error);
      });
    });

    describe('remove', function () {
      it('delete with _id, no children', function () {
        const targetCategory = Categories.findOne({});
        const removedCategories = Categories.methods.remove.call({
          selector: targetCategory._id,
        });

        assert.equal(removedCategories, 1);
        assert.isUndefined(Categories.findOne(targetCategory._id));
      });

      it('reject event with child because isRecursive is false', function () {
        // const targetCategory = Factory.create('event');
        // const newCategoryDoc = Factory.build('category');
        // newCategoryDoc.event = targetCategory;
        // Categories.insert(newCategoryDoc);

        // const removedEvents = Events.methods.remove.call({
        //   selector: targetCategory._id,
        // });

        // assert.equal(removedEvents, 0);
        // assert.isDefined(Events.findOne(targetCategory._id));
        assert.equal(0, 1);
      });

      /**
       *  Need CategoriesCollection stub to isolate Events
       */
      it('delete event with child because isRecursive is true', function () {
        // const targetCategory = Factory.create('event');

        // // Set 3 child Categories under Event
        // let newCategoryDoc;
        // newCategoryDoc = Factory.build('category');
        // newCategoryDoc.event = targetCategory;
        // Categories.insert(newCategoryDoc);
        // newCategoryDoc = Factory.build('category');
        // newCategoryDoc.event = targetCategory;
        // Categories.insert(newCategoryDoc);
        // newCategoryDoc = Factory.build('category');
        // newCategoryDoc.event = targetCategory;
        // Categories.insert(newCategoryDoc);

        // const removedEvents = Events.methods.remove.call({
        //   selector: targetCategory._id,
        //   isRecursive: true,
        // });

        // assert.equal(removedEvents, 1);
        // assert.equal(Categories.find({}).count(), 0);
        // assert.isUndefined(Events.findOne(targetCategory._id));
        assert.equal(0, 1);
      });

      it('reject non _.id selectors', function () {
        const targetCategory = Categories.findOne({});

        assert.throws(() => {
          Categories.methods.remove.call({
            selector: {
              acronym: targetCategory.acronym,
            },
          });
        }, Meteor.Error);
      });
    });
  });
});
