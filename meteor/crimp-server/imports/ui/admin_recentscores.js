import { Meteor } from 'meteor/meteor';
import { Template } from 'meteor/templating';
import { _ } from 'meteor/stevezhu:lodash';
import { moment } from 'meteor/momentjs:moment';

import RecentScores from '../data/recentscores';

import './admin_recentscores.html';


Meteor.subscribe('recentscoresToAdmin');


Template.admin_recentscores.helpers({
  recentscores: () => {
    const scores = RecentScores.find({}, {
      sort: { updated_at: -1 },
      limit: 2,
    }).fetch();

    const scoreArray = [];
    _.forEach(scores, (score) => {
      score.updated_at = moment(score.updated_at)
                          .fromNow();

      scoreArray.push(score);
    });

    return scoreArray;
  },
});
