import { Meteor } from 'meteor/meteor';
import { Template } from 'meteor/templating';
import { Roles } from 'meteor/alanning:roles';
import { Session } from 'meteor/session';
import { _ } from 'meteor/stevezhu:lodash';

import './admin_database.html';

Template.admin_database.helpers({
  admin_db_middle: () => (Session.get('admin_db_middle')
                          ? Session.get('admin_db_middle')
                          : 'admin_database_blank'),
  admin_db_right: () => (Session.set('admin_db_right')
                          ? Session.get('admin_db_right')
                          : 'admin_database_blank'),
});

Template.admin_db_menu.events({
  'click a.admin-db-menu'(event) {
    const dataAttr = event.currentTarget.dataset;
    Session.set('admin_db_middle', dataAttr.template);
  },
});



Meteor.subscribe('climbersToPublic', Session.get('viewCategoryId'));

Template.admin_db_climbers.events({
  climbers: () => {

  },
});



Meteor.subscribe('allUsersToAdmin');

Template.admin_db_users.helpers({
  users: () => Meteor.users.find({}),
});

Template.admin_db_users.events({
  'click a.admin-db-users-edit'(event) {
    const dataAttr = event.currentTarget.dataset;
    console.log(dataAttr.userId);
    // Session.set('admin_db_middle', );
  },
});
