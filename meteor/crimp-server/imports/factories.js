import { Factory } from 'meteor/dburles:factory';
import { Random } from 'meteor/random';
import { faker } from 'meteor/practicalmeteor:faker';

import Events from './data/events';
import Categories from './data/categories';
import Climbers from './data/climbers';
import Teams from './data/teams';


/**
 *  Time logic will not be tested, so all date related fields will
 *  be stubbed with `new Date()`
 */

Factory.define('event', Events, {
  event_name_full: faker.company.companyName(),
  event_name_short: 'event_name_short',
  time_start: new Date(),
  time_end: new Date(),
});

Factory.define('category', Categories, {
  category_name: faker.commerce.productName(),
  acronym: String(faker.random.uuid()).substr(0, 3),
  is_team_category: true,
  is_score_finalized: false,
  climber_count: 0,
  time_start: new Date(),
  time_end: new Date(),
  score_system: 'points',
  routes: [{
    _id: Random.id(),
    route_name: 'R1',
    score_rules: {
      points: 10,
    },
  }, {
    _id: Random.id(),
    route_name: 'R2',
    score_rules: {
      points: 10,
    },
  }, {
    _id: Random.id(),
    route_name: 'R3',
    score_rules: {
      points: 10,
    },
  }, {
    _id: Random.id(),
    route_name: 'R4',
    score_rules: {
      points: 10,
    },
  }, {
    _id: Random.id(),
    route_name: 'R5',
    score_rules: {
      points: 10,
    },
  }, {
    _id: Random.id(),
    route_name: 'R6',
    score_rules: {
      points: 10,
    },
  }],
  event: {},
});

Factory.define('category-ifsc', Categories, {
  category_name: faker.commerce.productName(),
  acronym: String(faker.random.uuid()).substr(0, 3),
  is_team_category: false,
  is_score_finalized: false,
  climber_count: 0,
  time_start: new Date(),
  time_end: new Date(),
  score_system: 'ifsc-top-bonus',
  routes: [{
    _id: Random.id(),
    route_name: 'R1',
    score_rules: {},
  }, {
    _id: Random.id(),
    route_name: 'R2',
    score_rules: {},
  }, {
    _id: Random.id(),
    route_name: 'R3',
    score_rules: {},
  }, {
    _id: Random.id(),
    route_name: 'R4',
    score_rules: {},
  }, {
    _id: Random.id(),
    route_name: 'R5',
    score_rules: {},
  }, {
    _id: Random.id(),
    route_name: 'R6',
    score_rules: {},
  }],
  event: {},
});

Factory.define('category-dummy', Categories, {
  category_name: faker.commerce.productName(),
  acronym: String(faker.random.uuid()).substr(0, 3),
  is_team_category: false,
  is_score_finalized: false,
  climber_count: 0,
  time_start: new Date(),
  time_end: new Date(),
  score_system: 'ifsc-top-bonus',
  routes: [{
    _id: Random.id(),
    route_name: 'R1',
    score_rules: {},
  }],
  event: {},
});

Factory.define('climber', Climbers, {
  climber_name: faker.name.findName(),
  identity: faker.phone.phoneNumberFormat(),
  affliation: faker.name.jobType(),
  categories: [],
});

Factory.define('team', Teams, {
  team_name: faker.company.catchPhrase(),
  category_id: 'category_id',
  climbers: [],
});
