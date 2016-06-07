import { FlowRouter } from 'meteor/kadira:flow-router';
import { BlazeLayout } from 'meteor/kadira:blaze-layout';

import '../imports/ui/crimp_spectator.js';

FlowRouter.route('/', {
  name: 'CRIMP Spectator',
  action() {
    BlazeLayout.render('crimp_spectator');
  },
});

FlowRouter.route('/admin', {
  name: 'CRIMP Admin Dashboard',
  action() {
    BlazeLayout.render('');
  },
});


FlowRouter.notFound = {
  action() {
    // TODO: Replace with an error page
    BlazeLayout.render('scoreboard');
  },
};
