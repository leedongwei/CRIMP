import { Template } from 'meteor/templating';
import { ReactiveVar } from 'meteor/reactive-var';
import { Messages } from '../imports/data/messages.js';

import './main.html';

Template.messages.onCreated(() => {

});

Template.messages.helpers({
  msgs() {
    return Messages.find({});
  },
});
