/**
 * Do not 'use strict' on this file
 * It prevents global variables
 */
import { Meteor } from 'meteor/meteor';

const CRIMP = {
  ENVIRONMENT: {
    /**
     *  Expected values: ['production', 'development']
     *  TODO: Remember to change when pushing to production
     */
    NODE_ENV: 'development',

    /**
     *  Demo mode will automatically set all new users as admins
     */
    DEMO_MODE: true,

    /**
     *  Full name for desktop views
     *  Recommended length: less than sixty characters
     *                 |-----------------this is 50 chars-----------------| */
    ORGANIZATION_NAME_FULL: 'CRIMP Development',

    /**
     *  Shortened name to be displayed on mobile screens
     *  Recommended length: less than 20 characters
     *                  |--this is 20 chars--|    */
    ORGANIZATION_NAME_SHORT: 'CRIMP-dev',
  },

  /**
   *  Groups for user roles for ease of use
   */
  ROLES: {
    admins: ['admin', 'hukkataival'],
    judges: ['judge', 'admin', 'hukkataival'],
    partners: ['partner', 'judge', 'admin', 'hukkataival'],
    strangers: ['denied', 'pending'],
  },
};


/**
 *  Ensure that process.env.NODE_ENV is not falsey
 *  isServer because client-side would die from process.env
 *  being undefined
 */
// TODO: REMOVED FOR QUICK WORK ON STAGING SERVER
// DONGWEI REMOVE BEFORE PRODUCTION!!!!!
// if (Meteor.isServer && process.env.NODE_ENV) {
//   CRIMP.ENVIRONMENT.NODE_ENV = process.env.NODE_ENV;
// }

export default CRIMP;
