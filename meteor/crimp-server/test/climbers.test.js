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

    // Creates a Climber document with a parent Category
    const newCategory = Factory.create('category');
    const newClimberDoc = Factory.build('climber');
    newClimberDoc.categories[0]._id = newCategory._id;
    Climbers.insert(newClimberDoc);
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
        const targetclimber = Climbers.findOne({});
        const removedClimbers = Climbers.methods.remove.call({
          selector: targetclimber._id,
        });

        assert.equal(removedClimbers, 1);
        assert.isUndefined(Climbers.findOne(targetclimber._id));
      });

      it('reject event with child because isRecursive is false', function () {
        // const targetclimber = Factory.create('event');
        // const newclimberDoc = Factory.build('climber');
        // newclimberDoc.event = targetclimber;
        // Categories.insert(newclimberDoc);

        // const removedEvents = Events.methods.remove.call({
        //   selector: targetclimber._id,
        // });

        // assert.equal(removedEvents, 0);
        // assert.isDefined(Events.findOne(targetclimber._id));
        assert.equal(0, 1);
      });

      it('delete event with child because isRecursive is true', function () {
        // const targetclimber = Factory.create('event');

        // // Set 3 child Categories under Event
        // let newclimberDoc;
        // newclimberDoc = Factory.build('climber');
        // newclimberDoc.event = targetclimber;
        // Categories.insert(newclimberDoc);
        // newclimberDoc = Factory.build('climber');
        // newclimberDoc.event = targetclimber;
        // Categories.insert(newclimberDoc);
        // newclimberDoc = Factory.build('climber');
        // newclimberDoc.event = targetclimber;
        // Categories.insert(newclimberDoc);

        // const removedEvents = Events.methods.remove.call({
        //   selector: targetclimber._id,
        //   isRecursive: true,
        // });

        // assert.equal(removedEvents, 1);
        // assert.equal(Categories.find({}).count(), 0);
        // assert.isUndefined(Events.findOne(targetclimber._id));
        assert.equal(0, 1);
      });

      it('reject non _.id selectors', function () {
        const targetclimber = Climbers.findOne({});

        assert.throws(() => {
          Climbers.methods.remove.call({
            selector: {
              acronym: targetclimber.acronym,
            },
          });
        }, Meteor.Error);
      });
    });
  });
});
