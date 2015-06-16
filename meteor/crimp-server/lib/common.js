// Initialize all global variables here
// Note: Collections are stored as global variables too
CRIMP = {};
CRIMP.schema = {};


// Note: User can modify this in the client console and get access to the
// template, but he will not be able to pull data from the server. This is
// not a security concern, just the way Meteor is designed.
CRIMP.roles = {
  // In increasing order of authority:
  // denied < pending < partner < judge < admin < hukkataival
  'unknown': ['denied', 'pending'],
  'partners': ['partner', 'judge', 'admin', 'hukkataival'],
  'organizers' : ['judge', 'admin', 'hukkataival'],
  'trusted': ['admin', 'hukkataival']
};
