import { Meteor } from 'meteor/meteor';
import { Roles } from 'meteor/alanning:roles';

/**
 *  Groups for user roles for ease of use
 *  Used in CRIMP.checkRoles below
 */
export const roles = {
  admins: ['admin', 'hukkataival'],
  judges: ['judge', 'admin', 'hukkataival'],
  partners: ['partner', 'judge', 'admin', 'hukkataival'],
  strangers: ['denied', 'pending'],
};


/**
 *  @param {Object[]} crimpRoles
 *  @param {Object} userObject
 *  @param {string} eventId
 *  @return {boolean}
 */
export function checkRoles(crimpRoles,
                           user = this.userId || Meteor.user(),
                           eventId = Roles.GLOBAL_GROUP) {
  if (!user) {
    throw new Meteor.Error(401, 'User has no login (CRIMP.checkRoles)');
  }

  if (!Roles.userIsInRole(user, crimpRoles, eventId)) {
    throw new Meteor.Error(403, 'User has no permissions (CRIMP.checkRoles)');
  }
}
