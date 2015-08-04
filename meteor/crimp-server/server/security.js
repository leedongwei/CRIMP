/**
 * MongoDB write-permissions from untrusted (client-side) code by
 * ongoworks:security https://github.com/ongoworks/meteor-security
 * Used in conjunction with aldeed:autoform
 *
 * DB operations from judges through REST API happens server-side
 * and will not be affected by this
 */
Categories.permit(['insert', 'update', 'remove'])
          .ifHasRole(CRIMP.roles.trusted)
          .apply();
Climbers.permit(['insert', 'update', 'remove'])
          .ifHasRole(CRIMP.roles.trusted)
          .apply();
Scores.permit(['insert', 'update', 'remove'])
          .ifHasRole(CRIMP.roles.trusted)
          .apply();
ActiveClimbers.permit(['insert', 'update', 'remove'])
          .ifHasRole(CRIMP.roles.trusted)
          .apply();
