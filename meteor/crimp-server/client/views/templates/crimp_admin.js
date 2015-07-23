Template.crimp_admin.onCreated(function() {
  document.title = "CRIMP Admin";

  // Shorten strings used by Moment.js
  moment.locale('en', {
    relativeTime : {
      future: "in %s",  past:   "%s",
      s:  "%dsec",
      m:  "a min",      mm: "%dmin",
      h:  "an hr",      hh: "%dhrs",
      d:  "a day",      dd: "%ddays",
      M:  "a mth",      MM: "%dmths",
      y:  "a yr",       yy: "%dyrs"
    }
  });

  // For switching between templates
  Session.setDefault('adminSectionView', 'admin_dashboard');
});

Template.crimp_admin.helpers({
  isLoggedIn: function() {
    return Roles.userIsInRole(Meteor.user(), CRIMP.roles.getAll);
  },
  isVerified: function() {
    // Note: A user can modify CRIMP.roles values in the client console
    // and get access to the template, but he will not be able to pull data
    // from the server. This is not a security concern, just the way Meteor
    // is designed.
    return Roles.userIsInRole(Meteor.user(), CRIMP.roles.partners);
  },
  adminSectionView: function() {
    return Session.get('adminSectionView');
  }
});

Template.crimp_admin.events({
  'click #crimp-admin-link-dashboard': function(event, template) {
    Session.set('adminSectionView', 'admin_dashboard');
  },
  'click #crimp-admin-link-database': function(event, template) {
    Session.set('adminSectionView', 'admin_database');
  }
})


Template.admin_dashboard.onCreated(function() {
  Tracker.autorun(function() {
    Meteor.subscribe('adminActiveClimbers');
    Meteor.subscribe('adminPendingJudges');
    Meteor.subscribe('adminRecentScores');
    Meteor.subscribe('adminAllUsers');
  });
});


Template.admin_dashboard.helpers({
  adminActiveClimbers: function() {
    return ActiveClimbers.find({})
            .fetch()
            .sort(adminActiveClimberSort);
  },
  adminPendingJudges: function() {
    return Roles.getUsersInRole('pending');
  },
  adminRecentScores: function() {
    var rs = RecentScores.find({}).fetch();

    // Most recent on top
    rs.sort(recentScoreSort);

    for (var i=0; i < rs.length; i++) {
      rs[i]['updated_at'] = moment(rs[i].updated_at).fromNow();
    }

    return rs
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



Template.admin_database.onCreated(function() {
  Session.setDefault('adminUserForm', Meteor.userId());
  Session.setDefault('adminClimberCategory', 'UMQ');
  Session.setDefault('adminClimberForm', 'UMQ001');
  Session.setDefault('adminCategoryForm', 'UMQ');
});
Template.admin_database.helpers({
  getaScore: function() {
    return Scores.findOne({});
  }
});



Template.admin_db_users.helpers({
  users: function() {
    return Meteor.users
            .find({}, { sort: ['profile.name', 'asc'] });
  }
});
Template.admin_db_users.events({
  'click .admin-users-edit': function(event, template) {
    var user = event.target.getAttribute('data-userId');
    Session.set('adminUserForm', user);
  },
});
Template.admin_db_users_form.helpers({
  targetUser: function() {
    var user = Meteor.users.findOne(Session.get('adminUserForm'));

    return {
      'user_id': user._id,
      'name': user.profile.name,
      'user_role': user.roles[0]
    }
  },
  userSchema: function() {
    return CRIMP.schema.user;
  },
  userRoleOptions: function() {
    return [
      { label: 'Pending', value: 'pending' },
      { label: 'Denied', value: 'denied' },
      { label: 'Partner', value: 'partner' },
      { label: 'Judge', value: 'judge' },
      { label: 'Admin', value: 'admin' }
    ];
  }
});



Template.admin_db_climbers.helpers({
  categories: function() {
    return Climbers.find({
      'category_id': Session.get('adminClimberCategory')
    }).fetch();
  },
});
// Template.admin_db_climbers.events({
//   'click .admin-categories-edit': function(event, template) {
//     var category = event.target.getAttribute('data-categoryId');
//     Session.set('adminCategoryForm', category);
//   },
// });
// Template.admin_db_climbers_form.helpers({
//   updateDocClimber: function() {
//     return Categories.findOne({
//       'category_id': Session.get('adminCategoryForm') || 'UMQ'
//     });
//   }
// });



Template.admin_db_categories.helpers({
  categories: function() {
    return Categories.find({}).fetch();
  },
});
Template.admin_db_categories.events({
  'click .admin-categories-edit': function(event, template) {
    var category = event.target.getAttribute('data-categoryId');
    Session.set('adminCategoryForm', category);
  },
});
Template.admin_db_categories_form.helpers({
  targetCategory: function() {
    return Categories.findOne({
      'category_id': Session.get('adminCategoryForm')
    });
  }
});



/*
 * Utility functions
 */
function adminActiveClimberSort (a, b) {
  return a.route_id > b.route_id ? 1 : -1;
}
function recentScoreSort(a, b) {
  return a.updated_at >= b.updated_at ? -1 : 1;
}
