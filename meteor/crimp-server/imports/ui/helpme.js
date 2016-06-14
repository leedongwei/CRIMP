import { Meteor } from 'meteor/meteor';
import { Template } from 'meteor/templating';
import { _ } from 'meteor/stevezhu:lodash';
import { sAlert } from 'meteor/juliancwirko:s-alert';

import HelpMe from '../data/helpme';
import CRIMP from '../settings';

import './helpme.html';


Meteor.subscribe('helpmeToAdmin');

// Hack to link sAlerts to its HelpMe
CRIMP.helpme = {};


Template.helpme.helpers({
  helpme: () => {
    const helpme = HelpMe.find({}).fetch();
    const alertsCreated = Object.keys(CRIMP.helpme)
                                .map(key => CRIMP.helpme[key]);

    _.forEach(helpme, (help) => {
      if (alertsCreated.indexOf(help._id) < 0) {
          // === undefined) {
        const display = '<span class="header">HELP ME!</span>'
                      + `<span class="name">${help.user_name}</span><br>`
                      + 'needs help at '
                      + `<span class="route">${help.route_name}</span>`;
        const alertId = sAlert.warning(display);

        CRIMP.helpme[alertId] = help._id;
      }
    });

    return helpme;
  },
});

Template.helpme.onRendered(() => {

});
