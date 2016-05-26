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
  assert.typeOf(category.climber_count, 'number');
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
    const newCategoryId = Categories.insert(newCategoryDoc);
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
        const parentEvent = Events.findOne({});
        const categoryDoc = Factory.tree('category-ifsc');

        const newCategoryId = Categories.methods.insert.call({
          parentEvent,
          categoryDoc,
        });

        const newCategory = Categories.findOne(newCategoryId);
        assertAllFields(newCategory);
      });

      it('reject extra fields', function () {
        const parentEvent = Events.findOne({});
        const categoryDoc = Factory.tree('category');
        categoryDoc.extra_field = true;

        assert.throws(() => {
          Categories.methods.insert.call({
            parentEvent,
            categoryDoc,
          });
        }, Meteor.Error);
      });

      it('reject missing fields', function () {
        const parentEvent = Events.findOne({});
        const categoryDoc = Factory.tree('category');
        delete categoryDoc.score_system;

        assert.throws(() => {
          Categories.methods.insert.call({
            parentEvent,
            categoryDoc,
          });
        }, Meteor.Error);
      });

      it('reject wrong types', function () {
        const parentEvent = Events.findOne({});
        const categoryDoc = Factory.tree('category');
        categoryDoc.is_score_finalized = 1;

        assert.throws(() => {
          Categories.methods.insert.call({
            parentEvent,
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

      it('reject category with child because isRecursive is false', function () {
        const targetCategory = Categories.findOne({});
        const newTeam = Factory.build('team');
        newTeam.category_id = targetCategory._id;
        Teams.insert(newTeam);

        const removedCategories = Categories.methods.remove.call({
          selector: targetCategory._id,
        });

        assert.equal(removedCategories, 0);
        assert.isDefined(Categories.findOne(targetCategory._id));
      });

      it('delete category with child because isRecursive is true', function () {
        const targetCategory = Categories.findOne({});
        const newTeam = Factory.build('team');
        newTeam.category_id = targetCategory._id;
        Teams.insert(newTeam);

        const removedCategories = Categories.methods.remove.call({
          selector: targetCategory._id,
          callback: null,
          isRecursive: true,
        });

        assert.equal(removedCategories, 1);
        assert.isUndefined(Categories.findOne(targetCategory._id));
      });

      it('reject non ._id selectors', function () {
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

    describe('Multi-collection functions', function () {
      beforeEach(function () {
        const targetCategory = Categories.findOne({});

        // Create a child Climber in the Category
        const newClimberDoc = Factory.build('climber');
        newClimberDoc.categories.push({ _id: targetCategory._id });
        const newClimberId = Climbers.insert(newClimberDoc);

        // Add Score for Climber in Category
        const newScoreDoc = {
          category_id: targetCategory._id,
          climber_id: newClimberId,
          marker_id: 'fake',
          scores: [],
        };
        Scores.insert(newScoreDoc);
      });

      describe('add Climber', function () {
        it('add to a valid category', function () {
          const targetCategory = Categories.findOne({});
          const newClimberDoc = Factory.tree('climber');
          const newClimberId = Climbers.insert(newClimberDoc);

          const scoreId = Categories.methods.addClimber.call({
            categoryId: targetCategory._id,
            climberId: newClimberId,
          });

          const testCategory = Categories.findOne(targetCategory._id);
          const testClimber = Climbers.findOne(newClimberId);
          const testScore = Scores.findOne(scoreId);

          assert.equal(
            testCategory.climber_count,
            targetCategory.climber_count + 1
          );
          assert.equal(
            testClimber.categories[0]._id,
            targetCategory._id
          );
          assert.equal(testScore.scores.length, testCategory.routes.length);
        });

        it('reject if there is an existing link', function () {
          const targetCategory = Categories.findOne({});
          const targetClimber = Climbers.findOne({});
          const targetScores = Scores.findOne({});

          // Should not be able to add a Climber to a Category twice
          assert.throws(() => {
            Categories.methods.addClimber.call({
              categoryId: targetCategory._id,
              climberId: targetClimber._id,
            });
          }, Meteor.Error);
        });
      });

      describe('remove Climber', function () {
        it('delete link in Climber and Scores', function () {
          const targetCategory = Categories.findOne({});
          const targetClimber = Climbers.findOne({});

          Categories.methods.removeClimbers.call({
            categoryId: targetCategory._id,
            climberId: targetClimber._id,
          });

          const testClimber = Climbers.findOne(targetClimber._id);
          assert.equal(testClimber.categories.length, 0);
          assert.isUndefined(Scores.findOne({
            climber_id: targetClimber._id,
            category_id: targetCategory._id,
          }));
        });

        it('no error if no linkage between Category and Climber', function () {
          const targetCategory = Categories.findOne({});
          const targetClimber = Factory.create('climber');

          assert.equal(Categories.methods.removeClimbers.call({
            categoryId: targetCategory._id,
            climberId: targetClimber._id,
          }), 0);
        });

        it('throw error for invalid Climber IDs', function () {
          const targetCategory = Categories.findOne({});

          assert.throws(() => {
            Categories.methods.removeClimbers.call({
              categoryId: targetCategory._id,
              climberId: 'Invalid Id',
            });
          }, Meteor.Error);
        });
      });
    });
  });
});
