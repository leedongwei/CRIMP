import { Meteor } from 'meteor/meteor';

// import CRIMP from '../imports/settings';
// import Messages from '../imports/data/messages';
// import Events from '../imports/data/events';
// import Categories from '../imports/data/categories';
// import Teams from '../imports/data/teams';
// import Climbers from '../imports/data/climbers';
// import Scores from '../imports/data/scores';
// import HelpMe from '../imports/data/helpme';
// import ActiveTracker from '../imports/data/activetracker';

import scanActiveTracker from '../imports/scanActiveTracker';
// import seedDatabase from '../imports/seedDatabase';

import IFSC_TB from '../imports/score_systems/ifsc-top-bonus';

Meteor.startup(() => {
  // Update ActiveTracker at regular intervals
  scanActiveTracker();

  const a = new IFSC_TB();
  console.log(a.calculate('111BBBTTT'));
  console.log(a.calculate('11'));
  console.log(a.calculate('T'));
  console.log(a.calculate('1B'));
  console.log(a.calculate('1TB'));
  console.log(a.calculate(''));

});
