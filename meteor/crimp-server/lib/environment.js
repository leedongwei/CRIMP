ENVIRONMENT = {  /**   * Ensure that process.env.NODE_ENV is not falsey   * Expected values are ['production', 'development']   */  NODE_ENV: process.env.NODE_ENV || 'development',  /**   *  Demo mode will automatically set all new users as admins   *  See server/application.js   */  DEMO_MODE: false}