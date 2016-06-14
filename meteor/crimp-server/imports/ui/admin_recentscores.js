import { Meteor } from 'meteor/meteor';
import { Template } from 'meteor/templating';
import { _ } from 'meteor/stevezhu:lodash';
import { moment } from 'meteor/momentjs:moment';

import RecentScores from '../data/recentscores';

import './admin_recentscores.html';


Meteor.subscribe('recentscoresToAdmin');


Template.admin_recentscores.helpers({
  recentscores: () => {
    const scores = RecentScores.find({})
                    .fetch()
                    .sort((a, b) => {
                      const timeA = Date.parse(a.updated_at);
                      const timeB = Date.parse(b.updated_at);

                      return timeA <= timeB ? -1 : 1;
                    });

    const scoreArray = [];
    _.forEach(scores, (score) => {
      score.updated_at = moment(score.updated_at)
                          .fromNow();

      if (scoreArray.length < 15) {
        scoreArray.push(score);
      } else {
        // Break for _.forEach
        return false;
      }
    });

    return scoreArray;
  },
});
