// Initialize all global variables here
// Note: Collections are stored as global variables too
CRIMP = {};
CRIMP.schema = {};

// In increasing order of authority:
// denied < pending < partner < judge < admin < hukkataival
CRIMP.roles = {
  'unknown': ['denied', 'pending'],
  'partners': ['partner', 'judge', 'admin', 'hukkataival'],
  'organizers' : ['judge', 'admin', 'hukkataival'],
  'trusted': ['admin', 'hukkataival']
}
