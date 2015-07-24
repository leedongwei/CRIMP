Template.crimp_spectator.helpers({
  isNotProduction: function() {
    return !ENVIRONMENT.production;
  },
  isDemo: function() {
    return ENVIRONMENT.demo;
  }
});
Template.crimp_spectator.rendered = function() {
  $(document).foundation('topbar');
};