import { Meteor } from 'meteor/meteor';
import { Session } from 'meteor/session';
import { Tracker } from 'meteor/tracker'
import { Template } from 'meteor/templating';
import { $ } from 'meteor/jquery';
import { _ } from 'meteor/stevezhu:lodash';
import { moment } from 'meteor/momentjs:moment';
// import { Foundation } from 'meteor/zurb:foundation-sites';
// import { ReactiveCountdown } from 'meteor/flyandi:reactive-countdown';

import Categories from '../data/categories';
import ActiveTracker from '../data/activetracker';

import './activetracker.html';

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
  categories: () => {
    const categories = Categories
                        .find({})
                        .fetch()
                        .sort((a, b) => {
                          const timeA = Date.parse(a.time_start);
                          const timeB = Date.parse(b.time_start);
                          if (timeA === timeB) return 0;
                          return timeA < timeB ? -1 : 1;
                        });

    _.forEach(categories, (c) => {
      c.time_start = moment(c.time_start).format('D MMM YY, H.mma');
      c.time_end = moment(c.time_end).format('D MMM YY, H.mma');
    });

    return categories;
  },
});

Template.activetracker.onCreated(() => {
  Meteor.subscribe('activetrackerToAll');
});
