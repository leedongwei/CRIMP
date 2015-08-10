Meteor.startup(function () {
  checkActiveClimber();
});

/**
 *  Find and remove any expired values in ActiveClimbers at a set interval
 */
function checkActiveClimber() {
  Meteor.setInterval(function() {
    // TODO: Remember to clean up and setInterval correctly
    console.log('--- ' + Date.now() + ' ---');

    ActiveMonitor.find({})
                 .forEach(checkActiveClimberExpiry);
  }, 1000);
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
    ActiveMonitor.remove(
      { route_id: ac.route_id },
      (error, result) => {
        if (error) {
          console.error(error);
        } else {
          console.log(`Removed ActiveMonitor: ${ac.route_id}`);
        }
      }
    );

    return;
  }

  if (ac.climber_id &&
      ac.climber_expiry < timeNow) {
    ActiveMonitor.update(
      { route_id: ac.route_id },
      { $set: {
        climber_id: '',
        climber_name: '',
      }},
      (error, results) => {
        if (error) {
          console.error(error);
        } else {
          console.log(`Removed ActiveClimber: ${ac.climber_name}`);
        }
      }
    );

    return;
  }
}
