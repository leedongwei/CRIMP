import { Meteor } from 'meteor/meteor';
import ActiveTracker from '../imports/data/activetracker';

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Interval-based functions                               *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
const SCAN_INTERVAL = 30 * 1000;

/**
 *  Checks admin and climber values to see if they are expired and remove
 *  them from the document
 *
 *  @param
 *    {object} ac - 1 document from ActiveMonitor
 */
function removeExpiredActiveTracker(trackerDoc) {
  const timeNow = Date.now();

  // Delete entire document if judge has expired
  if (trackerDoc.user_expiry < timeNow) {
    ActiveTracker.remove({ route_id: trackerDoc.route_id }, () => {});
    return;
  }

  // Remove climber's name if climber has expired
  // Skip update if there is no climber on the document
  if (trackerDoc.climber_id &&
      trackerDoc.climber_expiry < timeNow) {
    ActiveTracker.update(
      { route_id: trackerDoc.route_id },
      { $set: {
        climber_id: '',
        marker_id: '',
        climber_name: '',
      } },
      () => {}
    );

    return;
  }
}


/**
 *  Find and remove any expired values in ActiveClimbers at a set interval
 */
function scanActiveTracker() {
  Meteor.setInterval(() => {
    // TODO: Remove console.log
    console.log(`AT:  ${new Date(Date.now())}`);

    ActiveTracker.find({})
                 .forEach(removeExpiredActiveTracker);
  }, SCAN_INTERVAL);
}

export default scanActiveTracker;
