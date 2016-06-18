import { Meteor } from 'meteor/meteor';
import { Template } from 'meteor/templating';
import { _ } from 'meteor/stevezhu:lodash';
import { moment } from 'meteor/momentjs:moment';
// import { ReactiveCountdown } from 'meteor/flyandi:reactive-countdown';

import Categories from '../data/categories';

import './schedule.html';


Meteor.subscribe('categoriesToAll');


Template.schedule.helpers({
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
      const timeStart = Date.parse(c.time_start);
      const timeEnd = Date.parse(c.time_end);
      const timeNow = Date.now();
      c.start_in_hour = (timeNow - 60 * 60 * 1000 <= timeStart)
                        && (timeNow <= timeEnd) ? 'highlight' : '';

      c.time_start = moment(c.time_start).format('D MMM YY, H.mma');
      c.time_end = moment(c.time_end).format('D MMM YY, H.mma');
    });

    return categories;
  },
});
