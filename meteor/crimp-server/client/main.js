import { Meteor } from 'meteor/meteor';
import { Template } from 'meteor/templating';
import { ReactiveVar } from 'meteor/reactive-var';

import Messages from '../imports/data/messages';

import Events from '../imports/data/events';
import Categories from '../imports/data/categories';
import Teams from '../imports/data/teams';

import './main.html';


// TODO: Delete this crazy publication
Meteor.subscribe('development');
Meteor.subscribe('messages');
Meteor.subscribe('events');
Meteor.subscribe('categories');
Meteor.subscribe('teams');
Meteor.subscribe('climbers');
Meteor.subscribe('scores');

Template.messages.onCreated(() => {
  Events.remove({});
});

Template.messages.helpers({
  msgs() {
    return Messages.find({});
  },
});

