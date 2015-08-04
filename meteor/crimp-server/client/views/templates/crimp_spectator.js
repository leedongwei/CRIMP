Template.crimp_spectator.helpers({
  isNotProduction: function() {
    return ENVIRONMENT.NODE_ENV === 'production' ? true : false;
  },
  isDemo: function() {
    return ENVIRONMENT.DEMO_MODE;
  }
});

Template.crimp_spectator.rendered = function() {
  $(document).foundation('topbar');
};
