import { Meteor } from 'meteor/meteor';
import { Template } from 'meteor/templating';
// import { ReactiveCountdown } from 'meteor/flyandi:reactive-countdown';

import ActiveTracker from '../data/activetracker';

import './schedule.js';
import './activetracker.html';


Meteor.subscribe('activetrackerToAll');


Template.activetracker.helpers({
  activetracker: () => ActiveTracker
                        .find({})
                        .fetch()
                        .sort((a, b) => {
                          if (a.category_id === b.category_id) {
                            return a.route_name < b.route_name ? -1 : 1;
                          }
                          return a.category_name < b.category_name ? -1 : 1;
                        }),
});
