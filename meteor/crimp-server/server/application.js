Meteor.startup(function () {

});

Accounts.onCreateUser(function(options, user) {

  // Build the user's profile
  if (options.profile) {
    user.profile = options.profile;
  }

  // Auto-grant admin privileges for demo
  // TODO/Notes: Manually add role to user, because http://t.co/hg0K1NQdlU
  // There's probably a better way to do this, oh well.
  user.roles = ENVIRONMENT.demo ? 'admin' : 'pending';
  Roles.addUsersToRoles(user._id, user.roles);

  return user;
});