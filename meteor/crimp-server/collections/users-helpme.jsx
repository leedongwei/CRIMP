HelpMe = new Mongo.Collection('helpme');
CRIMP.schema.helpme = new SimpleSchema({
  route_id: {
    label: 'ID of route',
    type: String
  },
  admin_id: {
    label: 'ID of admin',
    type: String,
    optional: true
  },
  admin_name: {
    label: 'Name of admin',
    type: String,
    optional: true
  },
  updated_at: {
    type: Date,
    autoValue: function() {
      return new Date();
    }
  }
});


HelpMe.attachSchema(CRIMP.schema.helpme);

Meteor.methods({
  /**
   *  Admin acknowledges and removes the helpme request
   *
   *  @param
   *    {string} helpme_id - ID of helpme request
   */
  removeHelpMe(data) {
    CRIMP.checkPermission(CRIMP.roles.trusted);
    check(data, Object);
    check(data._id, String);

    HelpMe.remove({
      _id: data._id
    }, (error, result) => {});
  }
});