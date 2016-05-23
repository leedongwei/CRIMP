import { Factory } from 'meteor/dburles:factory';
import { Random } from 'meteor/random';
import { faker } from 'meteor/practicalmeteor:faker';

import Events from './data/events';
import Categories from './data/categories';


/**
 *  Time logic will not be tested, so all date related fields will
 *  be stubbed with `new Date()`
 */

Factory.define('event', Events, {
  _id: Random.id(),
  event_name_full: faker.company.companyName(),
  event_name_short: 'event_name_short',
  time_start: new Date(),
  time_end: new Date(),
});

Factory.define('category', Categories, {
  category_name: faker.commerce.productName(),
  acronym: String(faker.random.uuid).substr(0, 3),
  is_team_category: false,
  is_score_finalized: false,
  time_start: new Date(),
  time_end: new Date(),
  score_system: 'ifsc-top-bonus',
  routes: [{
    _id: Random.id(),
    route_name: 'R1',
    score_system_rules: {},
  }, {
    _id: Random.id(),
    route_name: 'R2',
    score_system_rules: {},
  }, {
    _id: Random.id(),
    route_name: 'R3',
    score_system_rules: {},
  }, {
    _id: Random.id(),
    route_name: 'R4',
    score_system_rules: {},
  }, {
    _id: Random.id(),
    route_name: 'R5',
    score_system_rules: {},
  }, {
    _id: Random.id(),
    route_name: 'R6',
    score_system_rules: {},
  }],
  event: {},
});


/*
const OMQ_BA2016 = Categories.insert({
  category_name: 'Open Men Qualifiers',
  acronym: 'OMQ',
  is_team_category: false,
  is_score_finalized: false,
  time_start: new Date(),
  time_end: new Date(),
  score_system: 'points',
  routes: [{
    _id: Random.id(),
    route_name: 'OMQ1',
    score_rules: {
      points: 1,
    },
  }, {
    _id: Random.id(),
    route_name: 'OMQ2',
    score_rules: {
      points: 10,
    },
  }, {
    _id: Random.id(),
    route_name: 'OMQ3',
    score_rules: {
      points: 100,
    },
  }, {
    _id: Random.id(),
    route_name: 'OMQ4',
    score_rules: {
      points: 1000,
    },
  }],
  event: {
    _id: BA2016,
    event_name_short: 'Boulderactive 2016',
  },
});
*/
