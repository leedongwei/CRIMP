Template.helpme.helpers({
  helpme: function() {
    return HelpMe.find({});
  }
});

Template.helpme_alert.onCreated(function() {
  $(document).foundation('alert', 'reflow');

  $(document).on('close.fndtn.alert', function(event) {
    var data = $(event.target).children('.close').data();

    console.log(data)

    Meteor.call('removeHelpMe', data);
  });
});