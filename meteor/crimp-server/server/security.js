import { Security } from 'meteor/ongoworks:security';
import { Roles } from 'meteor/alanning:roles';

import CRIMP from '../imports/settings';
// import Messages from '../imports/data/messages';
import Events from '../imports/data/events';
import Categories from '../imports/data/categories';
import Teams from '../imports/data/teams';
import Climbers from '../imports/data/climbers';
import Scores from '../imports/data/scores';
import HelpMe from '../imports/data/helpme';
import ActiveTracker from '../imports/data/activetracker';
import RecentScores from '../imports/data/recentscores';

Security.permit([
  'insert', 'update', 'remove',
]).collections([
  Events,
  Categories,
  Teams,
  Climbers,
  Scores,
  HelpMe,
  ActiveTracker,
  RecentScores,
]).ifHasRole({
  role: CRIMP.roles.judges,
  group: Roles.GLOBAL_GROUP,
}).allowInClientCode();
