// Note: This file deals with the Meteor.users collection

// Note: This is used to generate forms, it is not the full schema
// Form is at crimp_admin.html, template='admin_db_users_form'
CRIMP.schema.user = new SimpleSchema({
  user_id: {
    type: String
  },
  name: {
    type: String
  },
  user_role: {
    type: String
  }
});


Meteor.methods({
  changeUserRole: function(data) {
    if (!Roles.userIsInRole(Meteor.user(), CRIMP.roles.trusted)) {
      throw new Meteor.Error(403, "Access denied");
    }

    Roles.setUserRoles(data.user_id, [data.user_role]);
  }
});