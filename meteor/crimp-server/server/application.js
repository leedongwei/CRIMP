Meteor.startup(function () {

});

Accounts.onCreateUser(function(options, user) {
  // TODO:
  if (ENVIRONMENT.demo) {
    user.roles = ['admin'];
  } else {
    user.roles = ['pending'];
  }

  Roles.addUsersToRoles(user._id, user.roles);
  return user;
});