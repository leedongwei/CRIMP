import { Accounts } from 'meteor/accounts-base';

// TODO: Find a suitable home for this function
Accounts.onCreateUser((options, user) => {
  const createdUser = user;
  createdUser.services.resume = { loginTokensCount: 0 };
  return createdUser;
});
