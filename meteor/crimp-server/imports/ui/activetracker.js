import { Meteor } from 'meteor/meteor';
import { Session } from 'meteor/session';
import { Tracker } from 'meteor/tracker'
import { Template } from 'meteor/templating';
import { $ } from 'meteor/jquery';
import { _ } from 'meteor/stevezhu:lodash';
// import { Foundation } from 'meteor/zurb:foundation-sites';
// import { ReactiveCountdown } from 'meteor/flyandi:reactive-countdown';

import ActiveTracker from '../data/activetracker';

import './activetracker.html';

Template.activetracker.helpers({
  activetracker: () => {
    return ActiveTracker.find({})
                        .fetch()
                        .sort((a, b) => {
                          if (a.category_id === b.category_id) {
                            return a.route_name < b.route_name ? -1 : 1;
                          } else {
                            return a.category_name < b.category_name ? -1 : 1;
                          }
                        });
  },
});

Template.activetracker.onCreated(() => {
  Meteor.subscribe('activetrackerToAll');
});
