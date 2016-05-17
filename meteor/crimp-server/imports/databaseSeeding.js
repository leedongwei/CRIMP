import Events from './data/events.js';
import { Categories } from './data/categories';
import { Teams } from './data/teams';
import { Climbers } from './data/climbers';
import { Scores } from './data/scores.js';

// import {  } from './data/';

export const Seeds = {};

Seeds.newEvents = function () {
  Events.methods.insert.call({
    event_name_full: 'NUS - Black Diamond Boulderactive 2016',
    event_name_short: 'Boulderactive 2016',
    time_start: new Date(2016, 5, 16),
    time_end: new Date(2016, 5, 19),
  });
  Events.methods.insert.call({
    event_name_full: 'NUS - Black Diamond Boulderactive 2016',
    event_name_short: 'Boulderactive 2015',
    time_start: new Date(2016, 5, 16),
    time_end: new Date(2016, 5, 19),
  });
};

