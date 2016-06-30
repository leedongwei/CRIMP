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
      const targetClimber = Climbers.findOne({});
      assertAllFields(targetClimber);
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
        const targetClimber = Climbers.findOne({});
        const newclimberName = 'Updated Climber Name';

        Climbers.methods.update.call({
          selector: targetClimber._id,
          modifier: '$set',
          climberDoc: { climber_name: newclimberName },
        });

        assert.equal(
          Climbers.findOne(targetClimber._id).climber_name,
          newclimberName
        );
      });

      it('reject wrong types', function () {
        const targetClimber = Climbers.findOne({});
        const number = 123456;
        const string = 'true';

        // Try number in boolean
        assert.throws(() => {
          Climbers.methods.update.call({
            selector: targetClimber._id,
            modifier: '$set',
            eventDoc: { is_team_climber: number },
          });
        }, Meteor.Error);

        // Try string in boolean
        assert.throws(() => {
          Climbers.methods.update.call({
            selector: targetClimber._id,
            modifier: '$set',
            eventDoc: { is_score_finalized: string },
          });
        }, Meteor.Error);
      });
    });

    describe('remove', function () {
      it('delete with _id, no children', function () {
        const targetClimber = Factory.create('climber');
        const removedClimbers = Climbers.methods.remove.call({
          selector: targetClimber._id,
        });

        assert.equal(removedClimbers, 1);
        assert.isUndefined(Climbers.findOne(targetClimber._id));
      });

      it('reject climber with child because isRecursive is false', function () {
        const targetClimber = Climbers.findOne({});
        const removedClimbers = Climbers.methods.remove.call({
          selector: targetClimber._id,
        });

        assert.equal(removedClimbers, 0);
        assert.isDefined(Climbers.findOne(targetClimber._id));
      });

      it('delete climber with child because isRecursive is true', function () {
        const targetClimber = Climbers.findOne({});
        const removedClimbers = Climbers.methods.remove.call({
          selector: targetClimber._id,
          callback: null,
          isRecursive: true,
        });

        assert.equal(removedClimbers, 1);
        assert.isUndefined(Climbers.findOne(targetClimber._id));
      });

      it('reject non ._id selectors', function () {
        const targetClimber = Climbers.findOne({});

        assert.throws(() => {
          Climbers.methods.remove.call({
            selector: { acronym: targetClimber.acronym },
          });
        }, Meteor.Error);
      });
    });
  });
});
