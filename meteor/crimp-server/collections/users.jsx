/**
 *
 *  Note: This file deals with the Meteor.users collection
 *
 */

/**
 *  Note: This is used to auto-generate forms, it is not the full schema.
 *  Form is used at crimp_admin.html, template='admin_db_users_form'
 */
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
  /**
   *  Change the role of a user
   *
   *  @param
   *    {object} data
   *    {string} data.user_id     - target user to change role
   *    {string} data.user_role   - name of his new role
   *    {object} data.currentUser - TODO: check if this is needed
   */
  changeUserRole(data) {
    CRIMP.checkPermission(CRIMP.roles.trusted);
    check(data, Object);
    check(data.user_id, String);

    Roles.setUserRoles(data.user_id, [data.user_role]);
  },
});