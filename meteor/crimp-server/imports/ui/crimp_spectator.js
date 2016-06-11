import { Meteor } from 'meteor/meteor';
import { Template } from 'meteor/templating';
import { $ } from 'meteor/jquery';

import Events from '../data/events';

import './activetracker.js';
import './scoreboard.js';
import './crimp_spectator.html';


Meteor.subscribe('eventsToAll');


Template.crimp_spectator.helpers({
  event: () => Events.findOne({}),
});

Template.crimp_spectator.onRendered(() => {
  $('.top-bar').foundation();
});
