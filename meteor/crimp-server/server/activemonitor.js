Meteor.startup(function () {
  checkActiveClimber();
});

/**
 *  Find and remove any expired values in ActiveClimbers at a set interval
 */
function checkActiveClimber() {
  Meteor.setInterval(function() {
    // TODO: Remember to clean up
    // Frequency for testing async functions during development
    console.log('--- ' + Date.now() + ' ---');
    ActiveMonitor.find({})
                  .forEach(checkActiveClimberExpiry);
  }, 50000);
}

/**
 *  Checks admin and climber values to see if they are expired and remove
 *  them from the document
 *
 *  @param
 *    {object} ac - 1 document from ActiveMonitor
 */
function checkActiveClimberExpiry(ac) {
  var timeNow = Date.now();

  if (ac.admin_expiry < timeNow) {
    console.log('Removed ActiveAdmin: ${ac.admin_name}');

    ActiveMonitor.remove(ac._id, function(error, results) {
      if (error)  console.error(error);
    });
    return;
  }

  if (ac.climber_expiry < timeNow) {
    console.log('Removed ActiveMonitor: ${ac.climber_name}');
    CRIMP.activemonitor.removeActiveMonitor(ac._id);
    return;
  }
}

CRIMP.activemonitor = {
  insertActiveMonitor: function(selector, modifier) {
    if (!Roles.userIsInRole(Meteor.user(), CRIMP.roles.trusted)) {
      throw new Meteor.Error(403, "Access denied");
    }

    if (!modifier.climber_name) {

    }

    ActiveMonitor.upsert(selector,
      { $set: modifier },
      function(error, result) {
        // do nothing, prevents ActiveClimber.update from blocking
        if (error)  console.error(error);
      }
    );
  },
  removeActiveMonitor: function(selector, modifier) {
    modifier.climber_id = '';
    modifier.climber_name = '';
    // TODO: do checks


    ActiveMonitor.upsert(selector,
      { $set: modifier },
      function(error, result) {
        // do nothing, prevents ActiveClimber.update from blocking
        if (error)  console.error(error);
      }
    );
  }
}
