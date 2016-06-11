import { Meteor } from 'meteor/meteor';
import { Template } from 'meteor/templating';
import { $ } from 'meteor/jquery';

import Events from '../data/events';

import './scoreboard.js';
import './crimp_spectator.html';


Template.crimp_spectator.helpers({
  eventNameFull: () => Events.findOne({}).event_name_full,
  eventNameShort: () => Events.findOne({}).event_name_short,
});

Template.crimp_spectator.onCreated(() => {
  Meteor.subscribe('eventsToAll');
});

Template.crimp_spectator.onRendered(() => {
  $('.top-bar').foundation();
});
