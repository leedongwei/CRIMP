import { Meteor } from 'meteor/meteor';
import { Accounts } from 'meteor/accounts-base';
import { Roles } from 'meteor/alanning:roles';

import CRIMP from '../imports/settings';

Accounts.onCreateUser((options, user) => {
  const newUser = user;

  if (options.profile) {
    newUser.profile = options.profile;
    newUser.profile.link = user.services.facebook.link;
  }

  // Set count for sequential tokens
  newUser.services.resume = { loginTokensCount: 0 };

  // There are some issues with adding Roles during onCreateUser
  // See: https://github.com/alanning/meteor-roles#usage-examples
  const userRoles = [];
  if (Meteor.users.find().count() < 1) {
    userRoles.push('hukkataival');
  } else {
    userRoles.push(CRIMP.ENVIRONMENT.DEMO_MODE ? 'admin' : 'pending');
  }

  // Add to user object
  newUser.roles = { [Roles.GLOBAL_GROUP]: userRoles };

  // Add to Roles collection, try/catch for duplicate errors
  userRoles.forEach((role) => {
    try { Roles.createRole(role); } catch (e) { /* do nothing */ }
  });

  return newUser;
});
