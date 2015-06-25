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
      // expires in 5mins
      return new Date(Date.now() + 300000);
    }
  }
});


ActiveClimbers.attachSchema(CRIMP.schema.activeclimber);

if (Meteor.isServer) {
  Meteor.startup(function () {
    Meteor.setInterval(function() {
      console.log('   --- interval AC ---');
      ActiveClimbers.find({})
                    .forEach(checkActiveClimberExpiry);
    }, 90000);


  });


  function checkActiveClimberExpiry(ac) {
    var timeNow = Date.now();

    console.log(Date(timeNow) + ' checking AC\r\n'
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
      console.log('insertActiveClimber')
      console.log(selector)
      console.log(modifier)

      // TODO: do checks
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

      console.log('removeActiveClimber')
      console.log(selector)
      console.log(modifier)


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
