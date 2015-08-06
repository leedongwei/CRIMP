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
    ActiveClimbers.find({})
                  .forEach(checkActiveClimberExpiry);
  }, 50000);
}

/**
 *  Checks admin and climber values to see if they are expired and remove
 *  them from the document
 *
 *  @param
 *    {object} ac - 1 document from ActiveClimbers
 */
function checkActiveClimberExpiry(ac) {
  var timeNow = Date.now();

  if (ac.admin_expiry < timeNow) {
    console.log('Removed ActiveAdmin: ${ac.admin_name}');

    ActiveClimbers.remove(ac._id, function(error, results) {
      if (error)  console.error(error);
    });
    return;
  }

  if (ac.climber_expiry < timeNow) {
    console.log('Removed ActiveClimber: ${ac.climber_name}');
    CRIMP.activeclimbers.removeActiveClimber(ac._id);
    return;
  }
}

CRIMP.activeclimbers = {
  insertActiveClimber: function(selector, modifier) {
    if (!Roles.userIsInRole(Meteor.user(), CRIMP.roles.trusted)) {
      throw new Meteor.Error(403, "Access denied");
    }

    if (!modifier.climber_name) {

    }

    ActiveClimbers.upsert(selector,
      { $set: modifier },
      function(error, result) {
        // do nothing, prevents ActiveClimber.update from blocking
        if (error)  console.error(error);
      }
    );
  },
  removeActiveClimber: function(selector, modifier) {
    modifier.climber_id = '';
    modifier.climber_name = '';
    // TODO: do checks


    ActiveClimbers.upsert(selector,
      { $set: modifier },
      function(error, result) {
        // do nothing, prevents ActiveClimber.update from blocking
        if (error)  console.error(error);
      }
    );
  }
}
