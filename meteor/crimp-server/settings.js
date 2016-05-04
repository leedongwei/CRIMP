ENVIRONMENT = {
  /**
   *  Expected values: ['production', 'development']
   *  Remember to change when pushing to production
   */
  'NODE_ENV': 'development',

  /**
   *  Demo mode will automatically set all new users as admins
   */
  'DEMO_MODE': 'true',


  /**
   *  Full name for desktop views
   *  Recommended length: less than 60 characters
   */
  'EVENT_NAME_FULL': 'CRIMP Development',

  /**
   *  Shortened name for mobile screens
   *  You can reduce font-size for navbar to get more length
   *  Recommended length: less than 20 characters
   *                  |--this is 20 chars--|             */
  'EVENT_NAME_SHORT': 'CRIMP-dev',
};


/**
 *  Ensure that process.env.NODE_ENV is not falsey
 *  isServer because client-side would die from process.env
 *  being undefined
 */
if (Meteor.isServer && process.env.NODE_ENV) {
  ENVIRONMENT.NODE_ENV = process.env.NODE_ENV;
}
