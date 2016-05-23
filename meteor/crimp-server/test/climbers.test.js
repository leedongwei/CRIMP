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
import Categories from '../imports/data/categories';
import Teams from '../imports/data/teams';
import Climbers from '../imports/data/climbers';
import Scores from '../imports/data/scores';

if (!Meteor.isServer) {
  throw new Meteor.Error('not server');
}

function assertAllFields(climber) {
  assert.typeOf(climber, 'object');
  assert.typeOf(climber._id, 'string');
  assert.typeOf(climber.climber_name, 'string');
  assert.typeOf(climber.identity, 'string');
  assert.typeOf(climber.affliation, 'string');
  assert.typeOf(climber.categories, 'array');
  assert.typeOf(climber.updated_at, 'date');

  if (climber.categories.length) {
    assert.typeOf(climber.categories[0]._id, 'string');
    assert.typeOf(climber.categories[0].score_tiebreak, 'number');

    // Statuses are optional and not created during testing
    // assert.typeOf(climber.categories[0].status, 'string');
    // assert.typeOf(climber.categories[0].additional_status, 'string');
  }
}

describe('Climbers', function () {
  beforeEach(function () {
    resetDatabase();

    // Creates a Climber document with a parent Category and a child Score
    const newCategory = Factory.create('category');
    const newClimberDoc = Factory.build('climber');

    newClimberDoc.categories.push({ _id: newCategory._id });
    const newClimberId = Climbers.insert(newClimberDoc);

    const newScoreDoc = {
      category_id: newCategory._id,
      climber_id: newClimberId,
      marker_id: 'fake',
      scores: [],
    };
    Scores.insert(newScoreDoc);
  });


  describe('Mutator', function () {
    it('builds correctly from factory', function () {
      const targetclimber = Climbers.findOne({});
      assertAllFields(targetclimber);
    });
  });


  describe('Meteor.methods', function () {
    describe('insert', function () {
      it('insert with valid document', function () {
        const climberDoc = Factory.tree('climber');
        const newclimberId = Climbers.methods.insert.call(climberDoc);

        const newclimber = Climbers.findOne(newclimberId);
        assertAllFields(newclimber);
      });

      it('reject extra fields', function () {
        const climberDoc = Factory.tree('climber');
        climberDoc.extra_field = true;

        assert.throws(() => {
          Climbers.methods.insert.call(climberDoc);
        }, Meteor.Error);
      });

      it('reject missing fields', function () {
        const climberDoc = Factory.tree('climber');
        delete climberDoc.categories;

        assert.throws(() => {
          Climbers.methods.insert.call(climberDoc);
        }, Meteor.Error);
      });

      it('reject wrong types', function () {
        const climberDoc = Factory.tree('climber');
        climberDoc.categories = {};

        assert.throws(() => {
          Climbers.methods.insert.call(climberDoc);
        }, Meteor.Error);
      });
    });

    describe('update', function () {
      it('update with valid document', function () {
        const targetclimber = Climbers.findOne({});
        const newclimberName = 'Updated climber Name';

        Climbers.methods.update.call({
          selector: targetclimber._id,
          modifier: '$set',
          climberDoc: { climber_name: newclimberName },
        });

        assert.equal(
          Climbers.findOne(targetclimber._id).climber_name,
          newclimberName
        );
      });

      it('reject wrong types', function () {
        const targetclimber = Climbers.findOne({});
        const number = 123456;
        const string = 'true';

        // Try number in boolean
        assert.throws(() => {
          Climbers.methods.update.call({
            selector: targetclimber._id,
            modifier: '$set',
            eventDoc: { is_team_climber: number },
          });
        }, Meteor.Error);

        // Try string in boolean
        assert.throws(() => {
          Climbers.methods.update.call({
            selector: targetclimber._id,
            modifier: '$set',
            eventDoc: { is_score_finalized: string },
          });
        }, Meteor.Error);
      });
    });

    describe('remove', function () {
      it('delete with _id, no children', function () {
        const targetclimber = Factory.create('climber');
        const removedClimbers = Climbers.methods.remove.call({
          selector: targetclimber._id,
        });

        assert.equal(removedClimbers, 1);
        assert.isUndefined(Climbers.findOne(targetclimber._id));
      });

      it('reject event with child because isRecursive is false', function () {
        const targetclimber = Climbers.findOne({});
        const removedClimbers = Climbers.methods.remove.call({
          selector: targetclimber._id,
        });

        assert.equal(removedClimbers, 0);
        assert.isDefined(Climbers.findOne(targetclimber._id));
      });

      it('delete event with child because isRecursive is true', function () {
        const targetclimber = Climbers.findOne({});
        const removedClimbers = Climbers.methods.remove.call({
          selector: targetclimber._id,
          callback: null,
          isRecursive: true,
        });

        assert.equal(removedClimbers, 1);
        assert.isUndefined(Climbers.findOne(targetclimber._id));
      });

      it('reject non _.id selectors', function () {
        const targetclimber = Climbers.findOne({});

        assert.throws(() => {
          Climbers.methods.remove.call({
            selector: { acronym: targetclimber.acronym },
          });
        }, Meteor.Error);
      });
    });

    describe('add to Categories', function () {
      it('add to a valid category', function () {
        const targetCategory = Categories.findOne({});
        const newClimberDoc = Factory.tree('climber');
        const newClimberId = Climbers.insert(newClimberDoc);

        const scoreId = Climbers.methods.addToCategory.call({
          climberId: newClimberId,
          categoryId: targetCategory._id,
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

        // Should not be able to add a Climber to a Category twice
        assert.throws(() => {
          Climbers.methods.addToCategory.call({
            climberId: targetClimber._id,
            categoryId: targetCategory._id,
          });
        }, Meteor.Error);
      });
    });

    describe('remove from Categories', function () {
      it('delete link in Climber and Scores', function () {
        const targetCategory = Categories.findOne({});
        const targetClimber = Climbers.findOne({});

        Climbers.methods.removeFromCategory.call({
          climberId: targetClimber._id,
          categoryId: targetCategory._id,
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

        assert.equal(Climbers.methods.removeFromCategory.call({
          climberId: targetClimber._id,
          categoryId: targetCategory._id,
        }), 0);
      });

      it('throw error for invalid IDs', function () {
        const targetCategory = Categories.findOne({});

        assert.throws(() => {
          Climbers.methods.removeFromCategory.call({
            climberId: 'Invalid Id',
            categoryId: targetCategory._id,
          });
        }, Meteor.Error);
      });
    });
  });
});
