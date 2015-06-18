Restivus.configure({
  defaultHeaders: { 'Content-Type': 'application/json' },
  useAuth: true,
  // auth: {
  //   'token':
  //   'user':
  // }
  prettyJson: true
});

Restivus.addRoute('judge/login', { authRequired: false }, {
  post: function () {
    if (!('accessToken' in this.bodyParams) ||
        !('expiresAt' in this.bodyParams)) {
      return { 'error': 'Missing fields' };
    }

    var fb, user;

    try {
      // TODO: HTTP request will block. Find a better solution.
      fb = HTTP.call('GET',
            'https://graph.facebook.com/v2.3/me?access_token=' + this.bodyParams.accessToken,
            { 'timeout': 333 });

      // Append data not returned by API call
      fb.data.accessToken = this.bodyParams.accessToken;
      fb.data.expiresAt = this.bodyParams.expiresAt;

      user = Accounts.updateOrCreateUserFromExternalService(
        'facebook', fb.data
      );

      // Prevent multiple loginTokens on 1 account, doesn't seem to be needed
      // Accounts._clearAllLoginTokens(user.userId)

      // Create a loginToken and tie it to the account
      user.token = Accounts._generateStampedLoginToken();
      Accounts._insertLoginToken(user.userId, user.token);

      // Retrieve the role
      user.roles = Meteor.users.findOne(user.userId).roles;
      // TODO: Does not work: Roles.getRolesForUser(user.userId);


      return {
        'x-user-id': user.userId,
        'x-auth-token': user.token.token,
        'roles': user.roles
      };
    } catch (e) {
      return { error: e.message };
    }
  }
});