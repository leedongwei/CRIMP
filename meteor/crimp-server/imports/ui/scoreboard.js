import { Template } from 'meteor/templating';
import { $ } from 'meteor/jquery';
import { _ } from 'meteor/stevezhu:lodash';

import Categories from '../data/categories';
import Climbers from '../data/climbers';
import Scores from '../data/scores';
import TFBb from '../score_systems/top-flash-bonus2-bonus1';

import './scoreboard.html';

Template.scoreboard_header.helpers({
  currentCategories: () => {
    const currentCategories = [];
    const categories = Categories.find({}).fetch();

    _.forEach(categories, (c) => {
      currentCategories.push(c);
    });

    return currentCategories;
  },
  categories: () => Categories.find({}).fetch(),
});

Template.scoreboard_climbers.helpers({
  climbers: () => {
    const climbers = Climbers.find({}).fetch();
    const scoreSystem = new TFBb('test');
    const jointDocArray = [];

    climbers.forEach((climber) => {
      // FIXME: More than 1 scoring doc per climber
      const targetScore = Scores.findOne({
        climber_id: climber._id,
      });

      // Join Climber and Score documents for a category
      const jointDoc = scoreSystem.join(climber, targetScore);
      jointDoc.tabulatedScore = scoreSystem.tabulate(climber.scores);

      jointDocArray.push(jointDoc);
    });

    return scoreSystem.rankClimbers(jointDocArray);
  },
});

Template.crimp_spectator.onRendered(() => {
  $('.top-bar').foundation();
});
