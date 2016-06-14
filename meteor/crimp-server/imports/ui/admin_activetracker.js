import { Meteor } from 'meteor/meteor';
import { Template } from 'meteor/templating';
import { Roles } from 'meteor/alanning:roles';
import { Session } from 'meteor/session';
import { $ } from 'meteor/jquery';

import ActiveTracker from '../data/activetracker';
import CRIMP from '../settings';

import './admin_activetracker.html';

Meteor.subscribe('allUsersToAdmin');
Meteor.subscribe('activetrackerToAll');

Template.admin_activetracker.helpers({
  activetracker: () => ActiveTracker
                        .find({})
                        .fetch()
                        .sort((a, b) => {
                          if (a.category_id === b.category_id) {
                            return a.route_name < b.route_name ? -1 : 1;
                          }
                          return a.category_name < b.category_name ? -1 : 1;
                        }),
});

Template.admin_activetracker.events({
  'click a.admin-activeboard-remove'(event) {
    const dataAttr = event.currentTarget.dataset;

    ActiveTracker.methods.removeJudge.call({
      routeId: dataAttr.routeid,
      userId: Meteor.userId(),
    });
  },
});


Template.admin_permissions.helpers({
  adminPendingPermissions: () => Roles.getUsersInRole('pending').fetch(),
});

Template.admin_permissions.events({
  'click a.admin-permissions-approve'(event) {
    const dataAttr = event.currentTarget.dataset;
    const newRole = $(`#admin-permissions-${dataAttr.userid}`).val();

    Meteor.call('changeRole', {
      newRole,
      userId: dataAttr.userid,
    });
  },
});
