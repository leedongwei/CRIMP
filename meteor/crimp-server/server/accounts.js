import { Accounts } from 'meteor/accounts-base';

// TODO: Find a suitable home for this function
Accounts.onCreateUser((options, user) => {
  const newUser = user;

  if (options.profile) {
    newUser.profile = options.profile;
  }

  // Set count for sequential tokens
  newUser.services.resume = { loginTokensCount: 0 };

  return newUser;
});
