Meteor.startup(function () {

});

Accounts.onCreateUser(function(options, user) {
  // Build the user's profile
  if (options.profile) {
    user.profile = options.profile;
  }

  // Set first user as the boss
  if (Meteor.users.find().count() === 0) {
    user.roles = ['hukkataival'];
  } else {
    // Set users as admin if this is a demo
    user.roles = ENVIRONMENT.demo ? ['admin'] : ['pending'];
  }

  return user;
});
