/**
 *  Triggered AFTER creation of user account to add properties to the user
 *
 *  @param
 *    {object} options - should contain data from FB about the user
 *    {object} user - reference to the user object that was created
 */
Accounts.onCreateUser((options, user) => {
  // Build the user's profile
  if (options.profile) {
    user.profile = options.profile;
  }

  // Set first user as the boss
  if (Meteor.users.find().count() === 0) {
    user.roles = ['hukkataival'];

  } else {
    // Set users as admin if this is a demo
    user.roles = ENVIRONMENT.DEMO_MODE ? ['admin'] : ['pending'];
  }

  return user;
});
