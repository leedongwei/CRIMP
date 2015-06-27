if ('kadira' in Meteor.settings) {
  Kadira.connect(
    Meteor.settings.kadira.id,
    Meteor.settings.kadira.secret
  );
}