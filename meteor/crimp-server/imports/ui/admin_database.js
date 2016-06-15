import { Meteor } from 'meteor/meteor';
import { Template } from 'meteor/templating';
import { Roles } from 'meteor/alanning:roles';
import { Session } from 'meteor/session';
import { _ } from 'meteor/stevezhu:lodash';

import Categories from '../data/categories';
import Climbers from '../data/climbers';
import Scores from '../data/scores';

import './admin_database.html';

Template.admin_database.helpers({
  admin_db_middle: () => (Session.get('admin_db_middle')
                          ? Session.get('admin_db_middle')
                          : 'admin_database_blank'),
  admin_db_right: () => (Session.set('admin_db_right')
                          ? Session.get('admin_db_right')
                          : 'admin_database_blank'),
});

Template.admin_db_menu.events({
  'click a.admin-db-menu'(event) {
    const dataAttr = event.currentTarget.dataset;
    Session.set('admin_db_middle', dataAttr.template);
  },
});



Meteor.subscribe('categoriesToAll');

Template.admin_db_categories.helpers({
  categories: () => Categories.find({}).fetch(),
});



Meteor.subscribe('climbersToAdmin');
Meteor.subscribe('scoresToAdmin');

Template.admin_db_climbers.helpers({
  categories: () => Categories.find({})
                              .fetch()
                              .sort((a, b) => (a.acronym >= b.acronym
                                  ? -1
                                  : 1)),
  climbers: () => {
    const scores = Scores.find({
      category_id: Session.get('admin_db_climbers_category'),
    }).fetch();
    const climbers = [];

    _.forEach(scores, (score) => {
      const climber = Climbers.findOne(score.climber_id);
      climber.marker_id = score.marker_id;
      climber.scores = score.scores;

      climbers.push(climber);
    });

    return climbers.sort((a, b) => (a.marker_id < b.marker_id ? -1 : 1));
  },
});

Template.admin_db_climbers.events({
  'change .admin-db-climbers-categories'(event) {
    const categoryId = event.target.value;
    Session.set('admin_db_climbers_category', categoryId);
  },
  // 'click .admin-climbers-edit': function(event, template) {
  //   var climber = event.target.getAttribute('data-climberId');
  //   Session.set('templateRightColumn', 'admin_db_climbers_form');
  //   Session.set('adminClimberForm', climber);
  // },
  // 'click .admin-scores-edit': function(event, template) {
  //   var score = event.target.getAttribute('data-scoreId');
  //   Session.set('templateRightColumn', 'admin_db_scores_form');
  //   Session.set('adminScoreForm', score);
  // },
});


Meteor.subscribe('allUsersToAdmin');

Template.admin_db_users.helpers({
  users: () => Meteor.users.find({}),
});

Template.admin_db_users.events({
  'click a.admin-db-users-edit'(event) {
    const dataAttr = event.currentTarget.dataset;
    console.log(dataAttr.userId);
    // Session.set('admin_db_middle', );
  },
});
