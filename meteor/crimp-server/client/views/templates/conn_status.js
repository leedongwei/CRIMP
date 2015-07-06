Template.conn_status.helpers({
  state_check: function() {
    return Meteor.status();
  }
})