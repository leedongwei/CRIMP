import { Meteor } from 'meteor/meteor';
import { Session } from 'meteor/session';
import { Tracker } from 'meteor/tracker';
import { Template } from 'meteor/templating';
import { $ } from 'meteor/jquery';
import { _ } from 'meteor/stevezhu:lodash';
// import { Foundation } from 'meteor/zurb:foundation-sites';
// import { ReactiveCountdown } from 'meteor/flyandi:reactive-countdown';
import '../lib/jquery.kinetic.min.js';

import Categories from '../data/categories';
import Teams from '../data/teams';
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
    const categories = Categories.find({})
                        .fetch()
                        .sort((a, b) => (a.acronym >= b.acronym ? -1 : 1));

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
Template.scoreboard_scores.helpers({
  isTeamCategory: () => {
    const category = Categories.findOne(Session.get('viewCategoryId'));
    return category.is_team_category;
  },
});


/**
 *  Template functions for scoreboard_teams
 */
Template.scoreboard_teams.helpers({
  teams: () => {
    const category = Categories.findOne(Session.get('viewCategoryId'));
    if (!category.is_team_category) return [];

    const scoreSystem = getScoreSystem(category.score_system);
    const teams = Teams.find({
      category_id: Session.get('viewCategoryId'),
    }).fetch();


    // Build Team object by joining it with its child Climbers
    _.forEach(teams, (team) => {
      const climbers = [];

      // Tabulate scores for each individual climbers
      _.forEach(team.climbers, (climberId) => {
        const targetClimber = Climbers.findOne(climberId);
        const targetScore = Scores.findOne({
          climber_id: climberId,
          category_id: Session.get('viewCategoryId'),
        });

        const jointDoc = scoreSystem.join(targetClimber, targetScore);
        jointDoc.tabulatedScore = scoreSystem.tabulate(targetClimber.scores);

        climbers.push(jointDoc);
      });

      team.climbers = climbers;
      team.tabulatedScore = scoreSystem.tabulateTeam(team.climbers);
    });

    return scoreSystem.rankClimbers(teams);
  },
});


/**
 *  Template functions for scoreboard_climbers
 */
Template.scoreboard_climbers.helpers({
  climbers: () => {
    const category = Categories.findOne(Session.get('viewCategoryId'));
    if (category.is_team_category) return [];

    const climbers = Climbers.find({
      categories: { $elemMatch: {
        _id: Session.get('viewCategoryId'),
      } },
    }).fetch();
    const scoreSystem = getScoreSystem(category.score_system);
    const jointDocArray = [];


    _.forEach(climbers, (climber) => {
      const targetScore = Scores.findOne({
        category_id: Session.get('viewCategoryId'),
        climber_id: climber._id,
      });

      // Join Climber and Score documents for a category
      const jointDoc = scoreSystem.join(climber, targetScore);
      jointDoc.tabulatedScore = scoreSystem.tabulate(climber.scores);

      // Set to scoreboard to scrolling if there are more 6 routes
      jointDoc.needScrolling = targetScore.scores.length > 6;

      jointDocArray.push(jointDoc);
    });

    return scoreSystem.rankClimbers(jointDocArray);
  },
});


/**
 *  Template functions for scoreboard_climbers_item
 */
Template.scoreboard_climbers_item.onRendered(() => {
  const $table = $('.table-scrollable');
  if (!$table.length) {
    $('.climber-row')
      .children('.left-arrow')
        .hide()
      .children('.right-arrow')
        .hide();
    return;
  }

  // Resets all scroll watchers
  $table.off('scroll');

  // If there is a large table in the div
  if ($table.width() < $table.children('table').width()) {
    $('.climber-row .right-arrow').show();

    // jQuery Kinetic to allow drag-scrolling
    $table.kinetic({});

    // Change icon depending on mouseup/down
    $table.css('cursor', '-webkit-grab')
          .mouseup(() => {
            $table.css('cursor', '-webkit-grab');
          })
          .mousedown(() => {
            $table.css('cursor', '-webkit-grabbing');
          });

    // Show arrows depending on the position of the scrolling
    $table.scroll(function scrollingFunction() {
      const $this = $(this);

      if ($this.scrollLeft()
          === ($this.children('table').width() - $this.width())) {
        $this.parent().children('.right-arrow').hide();
      } else {
        $this.parent().children('.right-arrow').show();
      }

      if ($this.scrollLeft() === 0) {
        $this.parent().children('.left-arrow').hide();
      } else {
        $this.parent().children('.left-arrow').show();
      }
    });
  }
});
