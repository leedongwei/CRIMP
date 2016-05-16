import { Meteor } from 'meteor/meteor';
import { Accounts } from 'meteor/accounts-base';

// TODO: Find a suitable home for this function
Accounts.onCreateUser((options, user) => {
  const newUser = user;
  newUser.services.resume = { loginTokensCount: 0 };

  return newUser;
});
