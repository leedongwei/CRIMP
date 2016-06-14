import { Meteor } from 'meteor/meteor';
import { Template } from 'meteor/templating';
import { Roles } from 'meteor/alanning:roles';
import { Session } from 'meteor/session';

import Events from '../data/events';
import CRIMP from '../settings';

import './conn_status.js';
import './admin_dashboard.js';
import './crimp_admin.html';

Template.crimp_admin.helpers({
  event: () => Events.findOne({}),

  // Note: A user can modify CRIMP.roles values in the client console
  // and get access to the template, but he will not be able to pull data
  // from the server. This is not a security concern, just the way Meteor
  // is designed.
  isVerified: () => Roles.userIsInRole(Meteor.user(), CRIMP.roles.admins),

  currentAdminPage: () => (Session.get('currentAdminPage')
                      ? Session.get('currentAdminPage')
                      : 'admin_dashboard'),
});

