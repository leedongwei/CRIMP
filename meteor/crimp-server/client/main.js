import { Meteor } from 'meteor/meteor';
import { Session } from 'meteor/session';
import { Template } from 'meteor/templating';
import { Foundation } from 'meteor/zurb:foundation-sites';

import Messages from '../imports/data/messages';
import Events from '../imports/data/events';
import Categories from '../imports/data/categories';
import Teams from '../imports/data/teams';
import Climbers from '../imports/data/climbers';
import Scores from '../imports/data/scores';
import HelpMe from '../imports/data/helpme';
import ActiveTracker from '../imports/data/activetracker';

import TFBb from '../imports/score_systems/top-flash-bonus2-bonus1';

// TODO: REMOVE seedDatabase. DEV TESTING ONLY.
import CRIMP from '../imports/settings';


// TODO: Delete this crazy publication
Meteor.subscribe('development');
Meteor.subscribe('eventsToAll');
Meteor.subscribe('categoriesToAll');
Meteor.subscribe('teamsToAdmin');
Meteor.subscribe('climbersToAdmin');
Meteor.subscribe('scoresToAdmin');
Meteor.subscribe('activetrackerToAll');
Meteor.subscribe('helpmeToAdmin');
Meteor.subscribe('messagesToAdmin');
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


// const category = localStorage.getItem('currentCategory') || 'UMQ';
// Session.setDefault('currentCategory', category);
