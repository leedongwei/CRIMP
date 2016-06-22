import { Mongo } from 'meteor/mongo';
import { SimpleSchema } from 'meteor/aldeed:simple-schema';

import CRIMP from '../settings';

const RecentScores = new Mongo.Collection('RecentScores');

RecentScores.schema = new SimpleSchema({
  route_id: {
    type: String,
  },
  route_name: {
    type: String,
    label: 'Name of route',
  },
  user_id: {
    type: String,
  },
  user_name: {
    type: String,
    label: 'Name of judge on the route',
  },
  marker_id: {
    type: String,
    defaultValue: '',
    label: 'Marker ID of climber',
  },
  score_string: {
    label: 'Raw scoring string',
    type: String,
    trim: false,
  },
  updated_at: {
    type: Date,
    optional: true,   // optional to pass ValidatedMethod
    autoValue: () => new Date(),
  },
});
RecentScores.attachSchema(RecentScores.schema);

// if (CRIMP.ENVIRONMENT.NODE_ENV === 'production') {
//   RecentScores.deny({
//     insert() { return true; },
//     update() { return true; },
//     remove() { return true; },
//   });
// }

export default RecentScores;
