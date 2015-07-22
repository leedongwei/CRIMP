// Permissions by ongoworks:security
// https://github.com/ongoworks/meteor-security
Categories.permit(['insert', 'update', 'remove'])
          .ifHasRole(CRIMP.roles.trusted)
          .apply();
Climbers.permit(['insert', 'update', 'remove'])
          .ifHasRole(CRIMP.roles.trusted)
          .apply();
Scores.permit(['insert', 'update', 'remove'])
          .ifHasRole(CRIMP.roles.trusted)
          .apply();