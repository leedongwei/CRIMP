// Scores = new Mongo.Collection('score');
// Score_Schema = new SimpleSchema({
//   name: {
//     label: "Name of category",
//     type: String,
//     unique: true
//   },
//   acronym: {
//     label: "Acronym for name",
//     type: String,
//     index: true,
//     unique: true,
//     max: 3
//   },
//   route_count: {
//     type: Number,
//     label: "Number of routes",
//     min: 1
//   },
//   climbers: {
//     label: "References to climber documents",
//     // TODO: Change to Climber_Schema or array of sorts
//     type: String
//   },
//   time_start: {
//     label: "Starting date and time",
//     type: Date,
//     optional: true
//   },
//   time_end: {
//     label: "Ending date and time",
//     type: Date,
//     optional: true
//   }
// });



// Categories.attachSchema(Category_Schema);

// // TODO: Ensure admin-only access
// Meteor.methods({

// });