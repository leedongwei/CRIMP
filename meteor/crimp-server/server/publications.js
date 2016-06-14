import { Meteor } from 'meteor/meteor';
import { check } from 'meteor/check';

import CRIMP from '../imports/settings';
import Messages from '../imports/data/messages';
import Events from '../imports/data/events';
import Categories from '../imports/data/categories';
import Teams from '../imports/data/teams';
import Climbers from '../imports/data/climbers';
import Scores from '../imports/data/scores';
import HelpMe from '../imports/data/helpme';
import ActiveTracker from '../imports/data/activetracker';

/**
 *  White-list fields to expose for public subscriptions.
 *  Admin-only subscriptions will simply publish everything.
 */

// TODO: Delete this crazy publication and turn on permissions
Meteor.publish('development', () =>
               Meteor.users.find({}));

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Publications for core collections                      *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
Meteor.publish('eventsToAll',
              () => Events.find({}), {
                fields: {
                  _id: 1,
                  event_name_full: 1,
                  event_name_short: 1,
                  time_start: 1,
                  time_end: 1,
                  updated_at: 1,
                },
              });

Meteor.publish('categoriesToAll',
              () => Categories.find({}, {
                fields: {
                  _id: 1,
                  category_name: 1,
                  acronym: 1,
                  is_team_category: 1,
                  is_score_finalized: 1,
                  climber_count: 1,
                  time_start: 1,
                  time_end: 1,
                  score_system: 1,
                  routes: 1,
                  event: 1,
                  updated_at: 1,
                },
              }));

Meteor.publish('teamsToPublic',
              (categoryId) => {
                check(categoryId, String);
                return Teams.find({
                  category_id: categoryId
                }, {
                  fields: {
                    team_name: 1,
                    category_id: 1,
                    climbers: 1,
                    updated_at: 1,
                  },
                });
              });

Meteor.publish('teamsToAdmin',
              () => {
                // CRIMP.checkRoles(CRIMP.roles.admins);
                return Teams.find({});
              });

Meteor.publish('climbersToPublic',
              (categoryId) => {
                check(categoryId, String);
                return Climbers.find({
                  categories: { $elemMatch: {
                    _id: categoryId,
                  } },
                }, { fields: {
                  climber_name: 1,
                  gender: 1,
                  // identity: Hide for privacy issue
                  affliation: 1,
                  categories: 1,
                  updated_at: 1,
                } });
              });

Meteor.publish('climbersToAdmin',
              () => {
                // CRIMP.checkRoles(CRIMP.roles.admins);
                return Climbers.find({});
              });

Meteor.publish('scoresToPublic',
              (categoryId) => {
                check(categoryId, String);
                return Scores.find({
                  category_id: categoryId,
                }, {
                  fields: {
                    category_id: 1,
                    climber_id: 1,
                    marker_id: 1,
                    scores: 1,
                    updated_at: 1,
                  },
                });
              });

Meteor.publish('scoresToAdmin',
              () => {
                // CRIMP.checkRoles(CRIMP.roles.admins);
                return Scores.find({});
              });


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Publications for support collections                   *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
Meteor.publish('activetrackerToAll',
              () => ActiveTracker.find({}));

Meteor.publish('helpmeToAdmin',
              () => HelpMe.find({}));

Meteor.publish('messagesToAdmin',
              () => Messages.find({}));
