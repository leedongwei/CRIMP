import { Meteor } from 'meteor/meteor';
import { Mongo } from 'meteor/mongo';
import { ValidatedMethod } from 'meteor/mdg:validated-method';
import { SimpleSchema } from 'meteor/aldeed:simple-schema';

import CRIMP from '../settings';
// import Categories from './categories';
// import Climbers from './climbers';

const ActiveTracker = new Mongo.Collection('ActiveTracker');
ActiveTracker.schema = new SimpleSchema({
  route_id: {
    type: String,
    index: true,
    unique: true,
  },
  route_name: {
    type: String,
    label: 'Name of route',
  },
  category_id: {
    type: String,
  },
  category_name: {
    type: String,
    label: 'Name of category',
  },
  user_id: {
    type: String,
  },
  user_name: {
    type: String,
    label: 'Name of judge on the route',
  },
  user_expiry: {
    type: Date,
    label: 'Time to delete document',
    // expires in 20mins
    autoValue: () => new Date(Date.now() + (20 * 60000)),
  },
  climber_id: {
    type: String,
    defaultValue: '',
  },
  marker_id: {
    type: String,
    defaultValue: '',
    label: 'Marker ID of climber',
  },
  climber_name: {
    type: String,
    label: 'Name of climber',
    defaultValue: '',
  },
  climber_expiry: {
    label: 'Time to remove climber',
    type: Date,
    // expires in 10mins
    autoValue: () => new Date(Date.now() + (10 * 60000)),
  },
});
ActiveTracker.attachSchema(ActiveTracker.schema);

if (CRIMP.ENVIRONMENT.NODE_ENV === 'production') {
  ActiveTracker.deny({
    insert() { return true; },
    update() { return true; },
    remove() { return true; },
  });
}


ActiveTracker.methods = {};
// ActiveTracker.methods.addClimber = new ValidatedMethod({
//   name: 'ActiveTracker.methods.addClimber',
//   validate: new SimpleSchema({
//     routeId: { type: String },
//     climberId: { type: String },
//   }).validator(),
//   run({ routeId, climberId }) {

//   },
// });

// ActiveTracker.methods.removeClimber = new ValidatedMethod({
//   name: 'ActiveTracker.methods.removeClimber',
//   validate: new SimpleSchema({
//     routeId: { type: String },
//     climberId: { type: String },
//   }).validator(),
//   run({ routeId, climberId }) {

//   },
// });

// ActiveTracker.methods.addJudge = new ValidatedMethod({
//   name: 'ActiveTracker.methods.addJudge',
//   validate: new SimpleSchema({
//     routeId: { type: String },
//     userId: { type: String },
//   }).validator(),
//   run({ routeId, userId }) {

//   },
// });

// ActiveTracker.methods.removeJudge = new ValidatedMethod({
//   name: 'ActiveTracker.methods.removeJudge',
//   validate: new SimpleSchema({
//     routeId: { type: String },
//     userId: { type: String },
//   }).validator(),
//   run({ routeId, userId }) {

//   },
// });


export default ActiveTracker;


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *  Interval-based functions                               *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

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
export function runActiveTracker() {
  Meteor.setInterval(() => {
    // TODO: Remove console.log
    console.log(`AT:  ${new Date(Date.now())}`);

    ActiveTracker.find({})
                 .forEach(removeExpiredActiveTracker);
  }, 30 * 1000);
}

