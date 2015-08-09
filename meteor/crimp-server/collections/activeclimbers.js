ActiveMonitor = new Mongo.Collection('activemonitor');
CRIMP.schema.activemonitor = new SimpleSchema({
  route_id: {
    label: 'ID of route',
    type: String,
    index: true,
    unique: true
  },
  admin_id: {
    label: 'ID of admin',
    type: String
  },
  admin_name: {
    label: 'Name of admin',
    type: String
  },
  admin_expiry: {
    label: 'Time to delete document',
    type: Date,
    autoValue: function() {
      // expires in 15mins
      return new Date(Date.now() + 900000);
    }
  },
  climber_id: {
    label: 'ID of climber',
    type: String,
    optional: true
  },
  climber_name: {
    label: 'Name of climber',
    type: String,
    optional: true
  },
  climber_expiry: {
    label: 'Time to remove climber',
    type: Date,
    optional: true,
    autoValue: function() {
      // expires in 10mins
      return new Date(Date.now() + 600000);
    }
  }
});


ActiveMonitor.attachSchema(CRIMP.schema.activemonitor);

// if (Meteor.isServer) {
//   Server-side methods at ../server/activemonitor.js
// }

Meteor.methods({
  // data:string - route_id of any route on ActiveClimber
  removeActiveJudge: function(data) {
    if (!Roles.userIsInRole(Meteor.user(), CRIMP.roles.trusted)) {
      throw new Meteor.Error(403, "Access denied");
    }

    // TODO: Check data is actually a route
    ActiveMonitor.remove({ 'route_id': data });
  },


  removeActiveClimber: function(data) {
    // TODO
  },

  /**
   *
   *
   *  @param
   *    {string} data - category_id of any category
   */
  _initializeActiveAdmins: function(data) {
    CRIMP.checkPermission(CRIMP.roles.trusted);

    var category = Categories.findOne({category_id: data});
    if (!category) {
      console.info("category ${data} does not exist");
      return;
    }

    for (var i=1; i < category.route_count+1; i++) {
      CRIMP.activemonitor.insertActiveClimber(
        {
          'route_id': category.category_id + i.toString()
        },
        {
          'route_id': category.category_id + i.toString(),
          'climber_id': '',
          'climber_name': '',
          'admin_id': '0',
          'admin_name': '_initializeActiveMonitor'
        }
      );
    }
  },

  /**
   *
   *  @param
   *    data:string - category_id of any valid category
   */
  _insertActiveMonitor: function(data) {
    CRIMP.checkPermission(CRIMP.roles.trusted);

    var category = Categories.findOne({ 'category_id': data }),
        climbers = Climbers.find({}).fetch();

    if (!category)
      return { 'error': 'category_id \"' + data + '\" does not exist' };

    for (var i=1; i < category.route_count+1; i++) {
      CRIMP.activemonitor.insertActiveMonitor(
        {
          'route_id': category.category_id + i.toString()
        },
        {
          'route_id': category.category_id + i.toString(),
          'climber_id': climbers[i].climber_id,
          'climber_name': climbers[i].climber_name,
          'admin_id': '0',
          'admin_name': '_insertActiveMonitor'
        }
      );
    }
  }
})

