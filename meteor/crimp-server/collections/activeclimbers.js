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
    type: Number,
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
      return new Date(Date.now() + 15000);
    }
  }
});

ActiveClimbers.attachSchema(CRIMP.schema.activeclimber);

function checkActiveClimberExpiry(ac) {
  var timeNow = Date.now();

  console.log(timeNow + '/' + ac.climber_expiry + '/' + ac.admin_expiry);

  // if (ac.admin_expiry <= timeNow) {
  //   ActiveClimbers.remove(ac._id);
  //   return;
  // }

  if (ac.climber_expiry <= timeNow) {
    console.log('boomz')
    ActiveClimbers.update(ac._id, {
      climber_id: '',
      climber_expiry: ''
    });

    return;
  }
}

if (Meteor.isServer) {
  Meteor.startup(function () {
    // Check expiry every 90sec
    Meteor.setInterval(function() {
      console.log(' --- checking ---');
      var acCursor = ActiveClimbers.find({});
      acCursor.forEach(checkActiveClimberExpiry);
    }, 10000);
  });
}