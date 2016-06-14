import { Meteor } from 'meteor/meteor';
import { Template } from 'meteor/templating';

import './conn_status.html';

Template.conn_status.helpers({
  state_check: () => Meteor.status(),
});
