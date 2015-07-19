Template.crimp_admin.onCreated(function() {
  document.title = "CRIMP Admin";

  // Shorten strings used by Moment.js
  moment.locale('en', {
    relativeTime : {
      future: "in %s",  past:   "%s ago",
      s:  "%dsec",
      m:  "a min",      mm: "%dmin",
      h:  "an hr",      hh: "%dhrs",
      d:  "a day",      dd: "%ddays",
      M:  "a mth",      MM: "%dmths",
      y:  "a yr",       yy: "%dyrs"
    }
  });
});

Template.crimp_admin.helpers({
  isVerified: function() {
    // Note: A user can modify CRIMP.roles values in the client console
    // and get access to the template, but he will not be able to pull data
    // from the server. This is not a security concern, just the way Meteor
    // is designed.
    return Roles.userIsInRole(Meteor.user(), CRIMP.roles.partners);
  }
});


Template.admin_dashboard.onCreated(function() {
  Tracker.autorun(function() {
    Meteor.subscribe('adminActiveClimbers');
    Meteor.subscribe('adminPendingJudges');
    Meteor.subscribe('adminRecentScores');
  });


});


Template.admin_dashboard.helpers({
  adminActiveClimbers: function() {
    return ActiveClimbers.find({}).fetch();
  },
  adminPendingJudges: function() {
    return Roles.getUsersInRole('pending');
  },
  adminRecentScores: function() {
    var rs = RecentScores.find({}).fetch(),
        score;


    for (var i=0; i < rs.length; i++) {
      score = Scores.findOne({ '_id': rs[i].score_id });
      if (score) {
        rs[i]['route'] = score.route_id;
        rs[i]['climber'] = score.climber_id;
        rs[i]['score_string'] = score.score_string;
        rs[i]['updated'] = moment(rs[i].updated_at).fromNow();
      }
    }

    return rs;
  }
});

Template.admin_dashboard.events({
  'click .admin-activeboard-remove': function(event, template) {
    var route = event.target.getAttribute('data-routeId');
    Meteor.call('removeActiveJudge', route);
  },
  'click .admin-approvejudge-set': function(event, template) {
    var data = {};
        data['user_id'] = event.target.getAttribute('data-userId');
        data['user_role'] = $('#admin-approvejudge-' + data.user_id).val();

    Meteor.call('changeUserRole', data);
  }
});

Template.admin_database.helpers({
  getaScore: function() {
    return Scores.findOne({});
  }
});
