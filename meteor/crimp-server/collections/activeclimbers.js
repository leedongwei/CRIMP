ActiveClimbers = new Mongo.Collection('activeclimbers');
CRIMP.schema.activeclimber = new SimpleSchema({
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
      // expires in 10mins
      return new Date(Date.now() + 600000);
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
      // expires in 6mins
      return new Date(Date.now() + 360000);
    }
  }
});


ActiveClimbers.attachSchema(CRIMP.schema.activeclimber);

if (Meteor.isServer) {
  Meteor.startup(function () {
    // Meteor.setInterval(function() {

    //   // TODO: Clean up
    //   console.log('   --- interval AC ---');
    //   ActiveClimbers.find({})
    //                 .forEach(checkActiveClimberExpiry);
    // }, 90000);
  });


  function checkActiveClimberExpiry(ac) {
    var timeNow = Date.now();

    // TODO: Clean up
    console.log(Date(timeNow) + ' checking ActiveClimber\r\n'
      + ac.climber_expiry + ' | ' + ac.admin_expiry + '\r\n\r\n');

    if (ac.admin_expiry < timeNow) {
      ActiveClimbers.remove(ac._id, function(error, results) {
        // do nothing, prevents ActiveClimber.update from blocking
        if (error)  console.error(error);
      });
      return;
    }

    if (ac.climber_expiry < timeNow) {
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


      ActiveClimbers.update(selector,
        { $set: modifier },
        function(error, result) {
          // do nothing, prevents ActiveClimber.update from blocking
          if (error)  console.error(error);
        }
      );
    }
  }
}

Meteor.methods({
  // data:string - route_id of any route on ActiveClimber
  removeActiveJudge: function(data) {
    if (!Roles.userIsInRole(Meteor.user(), CRIMP.roles.trusted)) {
      throw new Meteor.Error(403, "Access denied");
    }

    // TODO: Check data is actually a route

    ActiveClimbers.remove({ 'route_id': data });
  },

  removeActiveClimber: function(data) {
    // TODO
  },

  // data:string - category_id of any valid category
  _insertActiveClimbers: function(data) {
    if (!Roles.userIsInRole(Meteor.user(), CRIMP.roles.trusted)) {
      throw new Meteor.Error(403, "Access denied");
    }

    var category = Categories.findOne({ 'category_id': data }),
        climbers = Climbers.find({}).fetch();

    if (!category)
      return { 'error': 'category_id \"' + data + '\" does not exist' };

    for (var i=1; i < category.route_count+1; i++) {
      CRIMP.activeclimbers.insertActiveClimber(
        {
          'route_id': category.category_id + i.toString()
        },
        {
          'route_id': category.category_id + i.toString(),
          'climber_id': climbers[i].climber_id,
          'climber_name': climbers[i].climber_name,
          'admin_id': '0',
          'admin_name': '_insertActiveClimbers'
        }
      );
    }
  },

  // data:string - category_id of any valid category
  _removeActiveClimbers: function(data) {
    if (!Roles.userIsInRole(Meteor.user(), CRIMP.roles.trusted)) {
      throw new Meteor.Error(403, "Access denied");
    }

    var category = Categories.findOne({ 'category_id': data });

    if (!category)
      return { 'error': 'category_id \"' + data + '\" does not exist' };

    for (var i=1; i < category.route_count+1; i++) {
      CRIMP.activeclimbers.removeActiveClimber(
        {
          'route_id': category.category_id + i.toString()
        },
        {
          'climber_id': '',
          'climber_name': '',
          'admin_id': '0',
          'admin_name': '_removeActiveClimbers'
        }
      );
    }
  }
})

