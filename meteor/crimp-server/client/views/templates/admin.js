Template.admin.helpers({
  isVerified: function() {
    // Note: A user can modify CRIMP.roles values in the console and get
    // access to the template, but he will not be able to pull data from
    // the server. This is not a security concern.
    return Roles.userIsInRole(Meteor.user(), CRIMP.roles.partners);
  }
});