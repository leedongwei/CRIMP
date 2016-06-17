import { Meteor } from 'meteor/meteor';
import { Template } from 'meteor/templating';
import { Roles } from 'meteor/alanning:roles';
import { Session } from 'meteor/session';
import { _ } from 'meteor/stevezhu:lodash';
import { SimpleSchema } from 'meteor/aldeed:simple-schema';
import { $ } from 'meteor/jquery';
import { AutoForm } from 'meteor/aldeed:autoform';
import { sAlert } from 'meteor/juliancwirko:s-alert';

import Categories from '../data/categories';
import Climbers from '../data/climbers';
import Scores from '../data/scores';
import Teams from '../data/teams';

import './admin_db_forms.html';

Template.admin_db_categories_form.helpers({
  formCategory: () => Categories,
  target: () => Categories.findOne(Session.get('admin_db_categories_form')),
});

Template.admin_db_categories_form.events({
  'click button'(event) {
    const formValues = AutoForm.getFormValues('updateCategory');
    let categoryDoc = formValues.updateDoc.$set;

    categoryDoc = _.pick(categoryDoc, [
      'category_name',
      'acronym',
      'is_score_finalized',
      'time_start',
      'time_end',
    ]);

    Categories.methods.update.call({
      selector: Session.get('admin_db_categories_form'),
      modifier: '$set',
      categoryDoc,
    });
  },
});

// admin_db_climbers_category
// admin_db_climbers_form
Template.admin_db_climbers_form.helpers({
  formClimber: () => Climbers,
  formScore: () => Scores,
  targetClimber: () => Climbers.findOne(Session.get('admin_db_climbers_form')),
  targetScore: () => {
    if (Session.get('admin_db_climbers_category')) {
      return Scores.findOne({
        climber_id: Session.get('admin_db_climbers_form'),
        category_id: Session.get('admin_db_climbers_category'),
      });
    }

    return null;
  },
  categories: () => Categories.find({})
                              .fetch()
                              .sort((a, b) => (a.acronym >= b.acronym
                                  ? -1
                                  : 1)),
});

Template.admin_db_climbers_form.events({
  // 'click .admin-db-climbers-form button'(event) {
  //   const formValues = AutoForm.getFormValues('updateClimber');
  //   let climberDoc = formValues.updateDoc.$set;

  //   categoryDoc = _.pick(climberDoc, [
  //     'climber_name',
  //     'identity',
  //     'gender',
  //     'affliation',
  //   ]);

  //   Climbers.methods.update.call({
  //     selector: Session.get('admin_db_climbers_form'),
  //     modifier: '$set',
  //     climberDoc,
  //   });
  // },

  'click .admin-db-scores-form button'(event) {
    const formValues = AutoForm.getFormValues('updateScore');
    let scoreDoc = formValues.updateDoc.$set;

    scoreDoc = _.pick(scoreDoc, [
      'marker_id',
      'scores',
      'scores.$.route_id',
      'scores.$.score_string',
    ]);

    // TODO: Use Validated Method
    Scores.update(Session.get('admin_db_climbers_form'), {
                    $set: scoreDoc,
                  });
  },

  'click .admin-db-climberToCategory-form button'(event) {
    const categoryId = $('#addClimberToCategory-selectCategoryId').val();
    const categoryText = $('#addClimberToCategory-selectCategoryId  option:selected ').text().trim();
    const markerId = $('#addClimberToCategory-inputMarkerId').val();
    const climberId = Session.get('admin_db_climbers_form');

    let hasAdded = false;
    try {
      hasAdded = Categories.methods.addClimber.call({
        climberId,
        categoryId,
        markerId,
      });
    } catch (e) {
      const display = `Error: ${e.message}`;
      sAlert.error(display, { timeout: 4500 });
    }

    if (hasAdded) {
      const scoreDoc = Scores.findOne(hasAdded);
      const display = `Added to <br><b>${categoryText}<b><br>`
                    + `as <b>${scoreDoc.marker_id}</b>`;
      sAlert.success(display, { timeout: 4500 });
    }
  },
});
