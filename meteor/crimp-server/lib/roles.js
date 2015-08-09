/**
 *  Note: User can modify this in the client console and get access to the
 *  template, but he will not be able to pull data from the server. This is
 *  not a security concern, just the way Meteor is designed.
 *
 *  In increasing order:
 *  denied < pending < partner < judge < admin < hukkataival
 */
CRIMP.roles = {
  // unknown has no access to anything
  unknown: ['denied', 'pending'],

  // partners have read-only access to all collections
  partners: ['partner', 'judge', 'admin', 'hukkataival'],

  // organizers have partner permissions + read+write access to scores
  organizers : ['judge', 'admin', 'hukkataival'],

  // trusted have read+write access to all collections
  trusted: ['admin', 'hukkataival'],

  // nalle is the man
  nalle: ['hukkataival'],

  all: [
    'denied',
    'pending',
    'partner',
    'judge',
    'admin',
    'hukkataival'
  ]
};


/**
 *  Checked if user is logged in and has appropriate permissions
 *
 *  @param
 *    {array} roles - Any of the groups above (e.g. CRIMP.roles.trusted)
 */
CRIMP.checkPermission = function(roles) {
  if (!Meteor.user() ||
      !Roles.userIsInRole(Meteor.user(), roles)) {
    throw new Meteor.Error(403, 'Access denied');
  }
};
