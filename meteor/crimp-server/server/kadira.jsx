/**
 *  Free monitoring tool: https://kadira.io/
 */
if ('kadira' in Meteor.settings) {
  Kadira.connect(
    Meteor.settings.kadira.id,
    Meteor.settings.kadira.secret
  );
}