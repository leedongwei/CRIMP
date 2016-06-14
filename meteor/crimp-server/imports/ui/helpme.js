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

    _.forEach(helpme, (help) => {
      const display = '<span class="header">HELP ME!</span>'
                    + `<span class="name">${help.user_name}</span><br>`
                    + 'needs help at '
                    + `<span class="route">${help.route_name}</span>`;
      const alertId = sAlert.warning(display);

      // Hack to link sAlerts to its HelpMe
      CRIMP.helpme[alertId] = help._id;
        console.log(CRIMP.helpme)

    });

    return helpme;
  },
});

Template.helpme.onRendered(() => {

});
