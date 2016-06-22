import { Meteor } from 'meteor/meteor';
import { Template } from 'meteor/templating';
import { _ } from 'meteor/stevezhu:lodash';
import { sAlert } from 'meteor/juliancwirko:s-alert';
import { moment } from 'meteor/momentjs:moment';

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
        const time = moment(Date.parse(help.updated_at)).format('h:mm:ss a');
        const display = '<span class="header">HELP ME!</span>'
                      + `<span class="name">${help.user_name}</span><br>`
                      + 'needs help at '
                      + `<span class="route">${help.route_name}</span><br>`
                      + `Reported at <span class="time">${time}</span>`;
        const alertId = sAlert.warning(display, { timeout: 0 });

        CRIMP.helpme[alertId] = help._id;
      }
    });

    return helpme;
  },
});

Template.helpme.onRendered(() => {

});
