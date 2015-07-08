Template.crimp_spectator.helpers({
  isNotProduction: function() {
    return !ENVIRONMENT.production;
  },
  isDemo: function() {
    return ENVIRONMENT.demo;
  }
});