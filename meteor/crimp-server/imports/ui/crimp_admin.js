import { Meteor } from 'meteor/meteor';
import { Template } from 'meteor/templating';
import { Roles } from 'meteor/alanning:roles';
import { Session } from 'meteor/session';
import { _ } from 'meteor/stevezhu:lodash';
import { sAlert } from 'meteor/juliancwirko:s-alert';

import Events from '../data/events';
import HelpMe from '../data/helpme.js';
import CRIMP from '../settings';

import './conn_status.js';
import './helpme.js';
import './admin_dashboard.js';
import './admin_database.js';
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
                      : 'admin_database'),
                      // : 'admin_dashboard'),
});

Template.crimp_admin.events({
  'click .currentAdminPage'(event) {
    const dataAttr = event.currentTarget.dataset;
    Session.set('currentAdminPage', dataAttr.pagename);
  },
});

Meteor.startup(() => {
  sAlert.config({
    effect: 'slide',
    position: 'top-right',
    timeout: 0,
    html: true,
    onRouteClose: true,
    stack: true,
    offset: 25,
    beep: false,
    onClose: () => {
      const alerts = sAlert.collection.find({}).fetch();
      const alertsArray = [];

      _.forEach(alerts, (a) => {
        alertsArray.push(a._id);
      });

      const deleted = _.difference(
                        Object.keys(CRIMP.helpme),
                        alertsArray
                      );

      _.forEach(deleted, (d) => {
        HelpMe.methods.remove.call({
          helpmeId: CRIMP.helpme[d],
          userId: Meteor.userId(),
        });

        _.unset(CRIMP.helpme, d);
      });
    },
  });
});
