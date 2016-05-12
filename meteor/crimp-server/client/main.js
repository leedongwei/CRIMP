import { Template } from 'meteor/templating';
import { ReactiveVar } from 'meteor/reactive-var';

import Messages from '../imports/data/messages.js';

import Events from '../imports/data/events.js';
import Categories from '../imports/data/categories.js';
import Teams from '../imports/data/teams.js';

import './main.html';

Template.messages.onCreated(() => {

});

Template.messages.helpers({
  msgs() {
    return Messages.find({});
  },
});
