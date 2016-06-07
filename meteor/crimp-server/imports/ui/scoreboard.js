import { Template } from 'meteor/templating';

import Categories from '../data/categories';
import Climbers from '../data/climbers';
import Scores from '../data/scores';
import TFBb from '../score_systems/top-flash-bonus2-bonus1';

import './scoreboard.html';

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
