import { Meteor } from 'meteor/meteor';

import CRIMP from '../imports/settings';
import Messages from '../imports/data/messages';
import Events from '../imports/data/events';
import Categories from '../imports/data/categories';
import Teams from '../imports/data/teams';
import Climbers from '../imports/data/climbers';
import Scores from '../imports/data/scores';
import HelpMe from '../imports/data/helpme';
import ActiveTracker from '../imports/data/activetracker';

Meteor.startup(() => {
  // TODO: Delete this crazy publication
  Meteor.publish('development', () => Meteor.users.find({}));

  /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
   *  Scoring Publications                                   *
   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */


  Meteor.publish('eventsToAll', () => Events.find({}));

  Meteor.publish('categoriesToAll', () => Categories.find({}));

  Meteor.publish('teamsToPublic', (categoryId) => Teams.find({}));
  Meteor.publish('teamsToAdmin', () => Teams.find({}));

  Meteor.publish('climbersToPublic',
                  (categoryId) => Climbers.find({
                    // only for a specific category
                  }, {
                    fields: {
                      identity: 0,
                    },
                  }));
  Meteor.publish('climbersToAdmin', () => Climbers.find({}));

  Meteor.publish('scoresToPublic', (categoryId) => Scores.find({}));
  Meteor.publish('scoresToAdmin', () => Scores.find({}));


  /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
   *  Support publications                                   *
   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
  Meteor.publish('activetrackerToAll', () => ActiveTracker.find({}));
  Meteor.publish('helpmeToAdmin', () => HelpMe.find({}));
  Meteor.publish('messagesToAdmin', () => Messages.find({}));
});
