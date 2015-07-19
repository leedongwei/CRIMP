// Note: This file contains functions that CRUD on the Meteor.user collection

Meteor.methods({
  changeUserRole: function(data) {
    if (!Roles.userIsInRole(Meteor.user(), CRIMP.roles.trusted) ||
        data.user_role == 'hukkataival') {
      throw new Meteor.Error(403, "Access denied");
    }

    Roles.setUserRoles(data.user_id, [data.user_role]);
  }
});