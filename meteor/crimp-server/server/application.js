Meteor.startup(function () {

});

Accounts.onCreateUser(function(options, user) {

  // Build the user's profile
  if (options.profile) {
    user.profile = options.profile;
  }

  // Auto-grant admin privileges for demo
  user.roles = ENVIRONMENT.demo ? 'admin' : 'pending';
  Roles.addUsersToRoles(user._id, user.roles);

  return user;
});