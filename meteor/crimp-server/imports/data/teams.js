import { Meteor } from 'meteor/meteor';
import { Mongo } from 'meteor/mongo';
import { ValidatedMethod } from 'meteor/mdg:validated-method';
import { SimpleSchema } from 'meteor/aldeed:simple-schema';

import CRIMP from '../settings';
import Categories from './categories';
import Climbers from './climbers';

const Teams = new Mongo.Collection('Teams');
Teams.schema = new SimpleSchema({
  team_name: {
    type: String,
  },
  category_id: {
    type: String,
    label: 'Reference to parent category',
  },
  climbers: {
    type: [String],
    label: 'References to climbers in team',
    defaultValue: [],
  },
  updated_at: {
    type: Date,
    optional: true,   // optional to pass ValidatedMethod
    autoValue: () => new Date(),
  },
});
Teams.attachSchema(Teams.schema);

if (CRIMP.ENVIRONMENT.NODE_ENV === 'production') {
  Teams.deny({
    insert() { return true; },
    update() { return true; },
    remove() { return true; },
  });
}


Teams.methods = {};
Teams.methods.insert = new ValidatedMethod({
  name: 'Teams.method.insert',
  validate: new SimpleSchema({
    parentCategory: { type: Object, blackbox: true },
    teamName: { type: String },
  }).validator(),
  run({ parentCategory, teamName }) {
    if (!parentCategory.is_team_category) {
      throw new Meteor.Error('IsNotTeamCategory');
    }

    const teamDoc = {
      team_name: teamName,
      category_id: parentCategory._id,
    };

    return Teams.insert(teamDoc);
  },
});

Teams.methods.update = new ValidatedMethod({
  name: 'Teams.method.update',
  validate: new SimpleSchema({
    selector: { type: String },
    modifier: { type: String },
    teamDoc: { type: Object },
    'teamDoc.team_name': { type: String, optional: true },
    'teamDoc.category_id': { type: String, optional: true },
  }).validator(),
  run({ selector, modifier, teamDoc }) {
    return Teams.update(selector, { [`${modifier}`]: teamDoc });
  },
});

Teams.methods.remove = new ValidatedMethod({
  name: 'Teams.method.remove',
  validate: new SimpleSchema({
    selector: { type: String },
    callback: { type: 'function', optional: true },
  }).validator(),
  run({ selector, callback }) {
    return Teams.remove({ _id: selector }, callback);
  },
});


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Multi-collections functions                            *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
Teams.methods.addClimber = new ValidatedMethod({
  name: 'Teams.methods.addClimber ',
  validate: new SimpleSchema({
    climberId: { type: String },
    teamId: { type: String },
  }).validator(),
  run({ teamId, climberId }) {
    const targetTeam = Teams.findOne(teamId);
    const targetClimber = Climbers.findOne(climberId);

    // Check if Climber is in Category
    let climberIsNotInCategory = true;
    for (let i = targetClimber.categories.length - 1; i >= 0; i--) {
      if (targetClimber.categories[i]._id === targetTeam.category_id) {
        climberIsNotInCategory = false;
      }
    }
    if (climberIsNotInCategory) {
      throw new Meteor.Error('ClimberIsNotInCategory');
    }

    // Check if Climber has been assigned to another Team
    const climberHasATeam = !Teams.methods.climberHasNoTeams.call({
      climberId,
      categoryId: targetTeam.category_id,
    });
    if (climberHasATeam) {
      throw new Meteor.Error('ClimberIsInATeam');
    }


    return Teams.update(teamId, {
      $addToSet: { climbers: climberId },
    });
  },
});

Teams.methods.removeClimber = new ValidatedMethod({
  name: 'Teams.methods.removeClimber',
  validate: new SimpleSchema({
    climberId: { type: String },
    teamId: { type: String },
  }).validator(),
  run({ teamId, climberId }) {
    return Teams.update(teamId, {
      $pull: { climbers: climberId },
    });
  },
});


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Utility functions                                      *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
/**
 *  Checks if Climber has been assigned to a team in a Category
 */
Teams.methods.climberHasNoTeams = new ValidatedMethod({
  name: 'Teams.methods.climberHasNoTeams',
  validate: new SimpleSchema({
    climberId: { type: String },
    categoryId: { type: String },
  }).validator(),
  run({ climberId, categoryId }) {
    return Teams.find({
      category_id: categoryId,
      climbers: { $in: [climberId] },
    }).count() === 0;
  },
});

export default Teams;
