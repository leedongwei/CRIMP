ActiveClimbers = new Mongo.Collection('categories');
CRIMP.schema.category = new SimpleSchema({
  route_id: {
    label: 'ID of route',
    type: String
  },
  admin_expiry: {
    label: 'Time to delete document',
    type: Date,
  },
  admin_id: {
    label: 'ID of admin',
    type: String
  },
  climber_id: {
    label: 'ID of climber',
    type: Number,
    optional: true
  },
  climber_expiry {
    label: 'Time to remove climber',
    type: Date,
    optional: true
  }
});

Categories.attachSchema(CRIMP.schema.category);