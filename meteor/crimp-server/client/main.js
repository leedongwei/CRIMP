import { Meteor } from 'meteor/meteor';
import { Session } from 'meteor/session';
import { Template } from 'meteor/templating';
import { Foundation } from 'meteor/zurb:foundation-sites';

// import Messages from '../imports/data/messages';
import Events from '../imports/data/events';
import Categories from '../imports/data/categories';
import Teams from '../imports/data/teams';
import Climbers from '../imports/data/climbers';
import Scores from '../imports/data/scores';
// import HelpMe from '../imports/data/helpme';
// import ActiveTracker from '../imports/data/activetracker';

import IFSC_TB from '../imports/score_systems/ifsc-top-bonus.js';
import TFBb from '../imports/score_systems/top-flash-bonus2-bonus1';
import CRIMP from '../imports/settings';


// window.Events = Events;
// window.Categories = Categories;
// window.Teams = Teams;
// window.Climbers = Climbers;
// window.Scores = Scores;
// window.CRIMP = CRIMP;


// Utility templates
import '../imports/ui/loader.js';

// ScoreSystem templates
import '../imports/score_systems/score-system-html-templates.js';

// Main templates
import '../imports/ui/crimp_spectator.js';
import '../imports/ui/crimp_admin.js';
