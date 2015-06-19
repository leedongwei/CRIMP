// Initialize all global variables here
// Note: Collections are stored as global variables too
CRIMP = {};
CRIMP.schema = {};


// Note: User can modify this in the client console and get access to the
// template, but he will not be able to pull data from the server. This is
// not a security concern, just the way Meteor is designed.
//
// In increasing order of authority:
// denied < pending < partner < judge < admin < hukkataival
CRIMP.roles = {
  // unknown has no access to anything
  'unknown': ['denied', 'pending'],

  // partners have read-only access to climbers and scores
  'partners': ['partner', 'judge', 'admin', 'hukkataival'],

  // organizers have read+write access to climbers and scores
  'organizers' : ['judge', 'admin', 'hukkataival'],

  // trusted have read+write access to categories, climbers, scores and users
  'trusted': ['admin', 'hukkataival']
};
