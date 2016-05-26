import { Meteor } from 'meteor/meteor';
import { Template } from 'meteor/templating';
import { ReactiveVar } from 'meteor/reactive-var';

import Messages from '../imports/data/messages';

import Events from '../imports/data/events';
import Categories from '../imports/data/categories';
import Teams from '../imports/data/teams';
import Climbers from '../imports/data/climbers.js';
import Scores from '../imports/data/scores.js';
import HelpMe from '../imports/data/helpme';
import ActiveTracker from '../imports/data/activetracker';

// TODO: REMOVE seedDatabase. DEV TESTING ONLY.
import seedDatabase from '../imports/seedDatabase';


import './main.html';


// TODO: Delete this crazy publication
Meteor.subscribe('development');
Meteor.subscribe('messages');
Meteor.subscribe('events');
Meteor.subscribe('categories');
Meteor.subscribe('teams');
Meteor.subscribe('climbers');
Meteor.subscribe('scores');
Meteor.subscribe('helpme');
Meteor.subscribe('activetracker');

Meteor.startup(() => {
  msg = Messages;
  eve = Events;
  cat = Categories;
  tms = Teams;
  cmb = Climbers;
  scs = Scores;
  hlp = HelpMe;
  act = ActiveTracker;
})

Template.messages.onCreated(() => {
  Events.remove({});
});

Template.messages.helpers({
  msgs() {
    return Messages.find({});
  },
});

