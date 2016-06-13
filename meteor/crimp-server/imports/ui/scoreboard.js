import { Meteor } from 'meteor/meteor';
import { Session } from 'meteor/session';
import { Tracker } from 'meteor/tracker'
import { Template } from 'meteor/templating';
import { $ } from 'meteor/jquery';
import { _ } from 'meteor/stevezhu:lodash';
// import { Foundation } from 'meteor/zurb:foundation-sites';
// import { ReactiveCountdown } from 'meteor/flyandi:reactive-countdown';

import Categories from '../data/categories';
import Climbers from '../data/climbers';
import Scores from '../data/scores';

import { scoreSystemsNames } from '../score_systems/score-system.js';
import IFSC_TB from '../score_systems/ifsc-top-bonus';
import TFBb from '../score_systems/top-flash-bonus2-bonus1';

import './scoreboard.html';


Meteor.subscribe('categoriesToAll');
Tracker.autorun(() => {
  Meteor.subscribe('teamsToPublic', Session.get('viewCategoryId'));
  Meteor.subscribe('climbersToPublic', Session.get('viewCategoryId'));
  Meteor.subscribe('scoresToPublic', Session.get('viewCategoryId'));
});


/**
 * Generic utility functions
 */
function updateViewCategory(categoryId) {
  Session.set('viewCategoryId', categoryId);
  localStorage.setItem('viewCategoryId', categoryId);
}
function checkIfOngoing(category) {
  const timeNow = Date.now();
  return category.time_start < timeNow && timeNow < category.time_end;
}
function getScoreSystem(scoreSystem) {
  let output;
  switch (scoreSystem) {
    case scoreSystemsNames[0]:
      output = new IFSC_TB();
      break;
    case scoreSystemsNames[1]:
      output = new TFBb();
      break;
    case scoreSystemsNames[2]:   // TODO: Set for points
      output = new TFBb();
      break;
    default:
      output = new IFSC_TB();
  }

  return output;
}


/**
 *  Template functions for scoreboard_header
 */
Template.scoreboard_header.helpers({
  categories: () => {
    const timeNow = Date.now();
    const categories = Categories.find({})
                        .fetch()
                        .sort((a, b) => {
                          return a.category_name <= b.category_name ? -1 : 1;
                        });

    _.forEach(categories, (c) => {
      c.isOngoing = checkIfOngoing(c);
    });

    return categories;
  },
  viewCategory: () => {
    if (!localStorage.getItem('viewCategoryId')) {
      // TODO: Set to the upcoming event
      localStorage.setItem('viewCategoryId', Categories.findOne({})._id);
    }

    if (!Session.get('viewCategoryId')) {
      updateViewCategory(localStorage.getItem('viewCategoryId'));
    }

    const category = Categories.findOne(Session.get('viewCategoryId'));
    category.isOngoing = checkIfOngoing(category);

    return category;
  },
});

Template.scoreboard_header.events({
  'click #scoreboard-selectCategory ul.menu li'(event) {
    const dataAttr = event.currentTarget.dataset;
    updateViewCategory(dataAttr.category);
  },
});

Template.scoreboard_header.onRendered(() => {
  $('.scoreboard-header').foundation();
});


/**
 *  Template functions for scoreboard_climbers
 */
Template.scoreboard_climbers.helpers({
  climbers: () => {
    const climbers = Climbers.find({
      categories: { $elemMatch: {
        _id: Session.get('viewCategoryId'),
      } },
    }).fetch();
    const category = Categories.findOne(Session.get('viewCategoryId'));
    const scoreSystem = getScoreSystem(category.score_system);
    const jointDocArray = [];

    climbers.forEach((climber) => {
      // FIXME: More than 1 scoring doc per climber
      const targetScore = Scores.findOne({
        climber_id: climber._id,
      });

      // Join Climber and Score documents for a category
      const jointDoc = scoreSystem.join(climber, targetScore);
      jointDoc.tabulatedScore = scoreSystem.tabulate(climber.scores);

      // Set to scoreboard to scrolling if there are more 6 routes
      jointDoc.needScrolling = targetScore.scores > 6;

      jointDocArray.push(jointDoc);
    });

    return scoreSystem.rankClimbers(jointDocArray);
  },
});

