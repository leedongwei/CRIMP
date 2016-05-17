import { Random } from 'meteor/random';
import Events from './data/events.js';
import Categories from './data/categories';
import Teams from './data/teams';
import Climbers from './data/climbers';
import Scores from './data/scores.js';

// import {  } from './data/';

export const Seeds = {};

Seeds.newEvents = function () {
  const BA2016 = Events.methods.insert.call({
    event_name_full: 'NUS - Black Diamond Boulderactive 2016',
    event_name_short: 'Boulderactive 2016',
    time_start: new Date(2016, 5, 16),
    time_end: new Date(2016, 5, 19),
  });
  const BA2015 = Events.methods.insert.call({
    event_name_full: 'NUS - Black Diamond Boulderactive 2016',
    event_name_short: 'Boulderactive 2015',
    time_start: new Date(2016, 5, 16),
    time_end: new Date(2016, 5, 19),
  });

  console.log(BA2016);
  console.log(BA2015);

  const NMQ_BA2016 = Categories.insert({
    category_name: 'Novice Men Qualifier',
    acronym: 'NMQ',
    is_team_category: false,
    score_finalized: false,
    time_start: new Date(),
    time_end: new Date(),
    score_system: 'ifsc-top-bonus',
    routes: [{
      _id: Random.id(),
      route_name: 'NMQ1',
    }, {
      _id: Random.id(),
      route_name: 'NMQ2',
    }, {
      _id: Random.id(),
      route_name: 'NMQ3',
    }, {
      _id: Random.id(),
      route_name: 'NMQ4',
    }, {
      _id: Random.id(),
      route_name: 'NMQ5',
    }, {
      _id: Random.id(),
      route_name: 'NMQ6',
    }],
    event: {
      _id: BA2016,
      event_name_short: 'Boulderactive 2016',
    },
  });

  const IMQ_BA2016 = Categories.insert({
    category_name: 'Intermediate Men Qualifiers',
    acronym: 'IMQ',
    is_team_category: false,
    score_finalized: false,
    time_start: new Date(),
    time_end: new Date(),
    score_system: 'top-flash-bonus2-bonus1',
    routes: [{
      _id: Random.id(),
      route_name: 'IMQ1',
    }, {
      _id: Random.id(),
      route_name: 'IMQ2',
    }, {
      _id: Random.id(),
      route_name: 'IMQ3',
    }, {
      _id: Random.id(),
      route_name: 'IMQ4',
    }],
    event: {
      _id: BA2016,
      event_name_short: 'Boulderactive 2016',
    },
  });

  const OMQ_BA2016 = Categories.insert({
    category_name: 'Open Men Qualifiers',
    acronym: 'OMQ',
    is_team_category: false,
    score_finalized: false,
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
};

