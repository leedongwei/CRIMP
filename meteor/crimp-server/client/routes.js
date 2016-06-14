import { FlowRouter } from 'meteor/kadira:flow-router';
import { BlazeLayout } from 'meteor/kadira:blaze-layout';

FlowRouter.route('/', {
  name: 'CRIMP Spectator',
  action() {
    BlazeLayout.render('crimp_spectator');
  },
});

FlowRouter.route('/admin', {
  name: 'CRIMP Admin Dashboard',
  action() {
    BlazeLayout.render('crimp_admin');
  },
});


FlowRouter.notFound = {
  action() {
    // TODO: Replace with an error page
    BlazeLayout.render('crimp_spectator');
  },
};
