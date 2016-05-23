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

if (!Meteor.isServer) {
  throw new Meteor.Error('not server');
}

function assertAllFields(team) {
  assert.typeOf(team, 'object');
  assert.typeOf(team._id, 'string');
  assert.typeOf(team.category_id, 'string');
  assert.typeOf(team.climbers, 'array');

  if (team.climbers.length) {
    assert.typeOf(team.climbers[0], 'string');
  }
}

describe('Teams', function () {
  beforeEach(function () {
    resetDatabase();

    // Creates a Team document with a parent Category and a child Climber
    const newCategory = Factory.create('category');

    // Link Climber to Category
    const newClimberDoc = Factory.build('climber');
    newClimberDoc.categories.push({ _id: newCategory._id });
    const newClimberId = Climbers.insert(newClimberDoc);

    const newTeamDoc = Factory.build('team');
    newTeamDoc.category_id = newCategory._id;
    newTeamDoc.climbers.push(newClimberId);
    Teams.insert(newTeamDoc);
  });


  describe('Mutator', function () {
    it('builds correctly from factory', function () {
      const targetTeam = Teams.findOne({});
      assertAllFields(targetTeam);
    });
  });


  describe('Meteor.methods', function () {
    describe('insert', function () {
      it('insert with valid document', function () {
        const parentCategory = Categories.findOne({});

        const newTeamId = Teams.methods.insert.call({
          parentCategory,
          teamName: 'Testing Team',
        });

        const newTeam = Teams.findOne(newTeamId);
        assertAllFields(newTeam);
      });

      it('reject wrong types', function () {
        const parentCategory = Categories.findOne({});

        assert.throws(() => {
          Teams.methods.insert.call({
            parentCategory,
            teamName: true,
          });
        }, Meteor.Error);
      });
    });

    describe('update', function () {
      it('update with valid document', function () {
        const targetTeam = Teams.findOne({});
        const newTeamName = 'Updated Team Name';

        Teams.methods.update.call({
          selector: targetTeam._id,
          modifier: '$set',
          teamDoc: { team_name: newTeamName },
        });

        assert.equal(
          Teams.findOne(targetTeam._id).team_name,
          newTeamName
        );
      });

      it('reject wrong types', function () {
        const targetTeam = Teams.findOne({});
        const number = 123456;

        // Try number in string
        assert.throws(() => {
          Teams.methods.update.call({
            selector: targetTeam._id,
            modifier: '$set',
            teamDoc: { team_name: number },
          });
        }, Meteor.Error);
      });
    });

    describe('remove', function () {
      it('delete with _id', function () {
        const targetTeam = Teams.findOne({});
        const removedTeams = Teams.methods.remove.call({
          selector: targetTeam._id,
        });

        assert.equal(removedTeams, 1);
        assert.isUndefined(Teams.findOne(targetTeam._id));
      });

      it('reject non _.id selectors', function () {
        const targetTeam = Teams.findOne({});

        assert.throws(() => {
          Teams.methods.remove.call({
            category_id: targetTeam.category_id,
          });
        }, Meteor.Error);
      });
    });

    describe('add Climber', function () {
      it('add to a valid Team', function () {
        const targetCategory = Categories.findOne({});
        const targetTeam = Teams.findOne({});

        const newClimberDoc = Factory.build('climber');
        newClimberDoc.categories.push({ _id: targetCategory._id });
        const newClimberId = Climbers.insert(newClimberDoc);

        Teams.methods.addClimber.call({
          teamId: targetTeam._id,
          climberId: newClimberId,
        });

        const testTeam = Teams.findOne(targetTeam._id);
        assert.include(testTeam.climbers, newClimberId);
      });

      it('reject if Climber has a Team in same Category', function () {
        const targetTeam = Teams.findOne({});
        const targetClimber = Climbers.findOne({});

        assert.throws(() => {
          Teams.methods.addClimber.call({
            teamId: targetTeam._id,
            climberId: targetClimber._id,
          });
        }, Meteor.Error);
      });

      it('reject if Climber is not in Category', function () {
        const targetTeam = Teams.findOne({});
        const newClimber = Factory.create('climber');

        assert.throws(() => {
          Teams.methods.addClimber.call({
            teamId: targetTeam._id,
            climberId: newClimber._id,
          });
        }, Meteor.Error);
      });
    });

    describe('remove Climber', function () {
      it('delete link in Climber and Scores', function () {
        const targetTeam = Teams.findOne({});
        const targetClimber = Climbers.findOne({});

        const removedClimbers = Teams.methods.removeClimber.call({
          teamId: targetTeam._id,
          climberId: targetClimber._id,
        });

        assert.equal(removedClimbers, 1);
      });

      it('no error if Climber does not exist', function () {
        const targetTeam = Teams.findOne({});

        const updatedTeams = Teams.methods.removeClimber.call({
          teamId: targetTeam._id,
          climberId: 'fake climber_id',
        });

        assert.equal(updatedTeams, 1);
      });

      it('no error if Team does not exist', function () {
        const targetClimber = Climbers.findOne({});

        const updatedTeams = Teams.methods.removeClimber.call({
          teamId: 'fake team_id',
          climberId: targetClimber._id,
        });

        assert.equal(updatedTeams, 0);
      });
    });
  });
});
