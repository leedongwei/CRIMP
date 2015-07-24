Template.scoreboard.onCreated(function() {
  Tracker.autorun(function() {
    Meteor.subscribe('getCategories');
    Meteor.subscribe('getClimbers',
        { category_id: Session.get('currentCategory') });
    Meteor.subscribe('getScores',
        { category_id: Session.get('currentCategory') });
  });
});



/*
 *  Scoreboard Categories
 */
Template.scoreboard_header.helpers({
  categories: function() {
    return Categories.find({}).fetch();
  },
  currentCategory: function(category_id) {
    return Session.get('currentCategory');
  },
  isCurrentCategory: function(category_id) {
    return Session.equals('currentCategory', category_id);
  },
  isCurrentCategoryFinalized: function() {
    return Categories.findOne({
      'category_id': Session.get('currentCategory')
    }).scores_finalized;
  }
});

Template.scoreboard_header.rendered = function() {
  var displayCategory = $.cookie('currentCategory') || 'UMQ',
      displayName;

  // TODO: Bloody hack because of some sync issues at pageload
  // No time to figure out, hack first think later :X
  switch(displayCategory) {
    case 'UMQ':
      displayName = 'U-17 Men Qualifer';
      break;
    case 'UMF':
      displayName = 'U-17 Men Final';
      break;
    case 'UWQ':
      displayName = 'U-17 Women Qualifer';
      break;
    case 'UWF':
      displayName = 'U-17 Women Final';
      break;
    case 'NMQ':
      displayName = 'Novice Men Qualifer';
      break;
    case 'NMF':
      displayName = 'Novice Men Final';
      break;
    case 'NWQ':
      displayName = 'Novice Women Qualifer';
      break;
    case 'NWF':
      displayName = 'Novice Women Final';
      break;
    case 'IMQ':
      displayName = 'Intermediate Men Qualifer';
      break;
    case 'IMF':
      displayName = 'Intermediate Men Final';
      break;
    case 'IWQ':
      displayName = 'Intermediate Women Qualifer';
      break;
    case 'IWF':
      displayName = 'Intermediate Women Final';
      break;
    case 'OMQ':
      displayName = 'Open Men Qualifer';
      break;
    case 'OMF':
      displayName = 'Open Men Final';
      break;
    case 'OWQ':
      displayName = 'Open Women Qualifer';
      break;
    case 'OWF':
      displayName = 'Open Women Final';
      break;
    case 'VMQ':
      displayName = 'Invitational Men Qualifer';
      break;
    case 'VMF':
      displayName = 'Invitational Men Final';
      break;
    case 'VWQ':
      displayName = 'Invitational Women Qualifer';
      break;
    case 'VWF':
      displayName = 'Invitational Women Final';
      break;
  }

  $('#scoreboard-categories-link').html(
    displayName + '<i class="material-icons">more_vert</i>'
  );

  // Initialize Foundation plugin
  $(document).foundation('dropdown', 'reflow');
}

Template.scoreboard_header.events({
  'click .scoreboard-categories-options': function(event, template) {
    var category = event.target.getAttribute('data-categoryId');

    Session.set('currentCategory', category);
    $.cookie('currentCategory', category);

    $('#scoreboard-categories-link').html(
      Categories.findOne({ 'category_id': category }).category_name +
      '<i class="material-icons">more_vert</i>'
    )
  }
});



/*
 *  Scoreboard Climber
 */
Template.scoreboard_climbers.helpers({
  climbers: function() {
    var climberData = Climbers
                        .find({ category_id: Session.get('currentCategory') })
                        .fetch();

    // Case where there are no climbers in the system
    if (!climberData.length)  return climberData;

    // Tabulate scores for each climber
    climberData = climberTabulateScores(climberData);

    // Sort climbers
    climberData.sort(climberSort);

    // Rank climbers
    climberData = climberTabulateRanks(climberData);

    return climberData;
  }
});



/*
 *  Utility functions
 */
function climberTabulateScores(climberData) {
  climberData.forEach(function(climber, index) {
    var scoreArray = [],
        tops = 0,
        topAttempts = 0,
        bonuses = 0,
        bonusAttempts = 0;

    for (var i=1; i < Object.keys(climber.scores).length+1; i++) {
      var cs = Scores.findOne(climber.scores[i]);

      if (!cs)  continue;
      scoreArray.push(cs);

      // Tabulate succesful top/bonus
      if (cs.score_top)    tops++;
      if (cs.score_bonus)  bonuses++;

      // Tabulate top/bonus attempts
      // parseInt prevents NaN error
      if (cs.score_top === parseInt(cs.score_top)) {
        topAttempts += cs.score_top;
      }
      if (cs.score_bonus === parseInt(cs.score_bonus)) {
        bonusAttempts += cs.score_bonus;
      }
    }

    // Replace the scores._id array with the score documents array
    climber.scores = scoreArray;

    // Add in tabulated scores
    climber.tops = tops;
    climber.bonuses = bonuses;
    climber.topAttempts = topAttempts;
    climber.bonusAttempts = bonusAttempts;
  });

  return climberData
}

function climberSort(a, b) {
  if (a.tops !== b.tops)
    return a.tops > b.tops ? -1 : 1;

  if (a.topAttempts !== b.topAttempts)
    return a.topAttempts < b.topAttempts ? -1 : 1;

  if (a.bonuses !== b.bonuses)
    return a.bonuses > b.bonuses ? -1 : 1;

  if (a.bonusAttempts !== b.bonusAttempts)
    return a.bonusAttempts < b.bonusAttempts ? -1 : 1;

  if (a.scores_tiebreak !== b.scores_tiebreak)
    return a.scores_tiebreak > b.scores_tiebreak ? -1 : 1;

  return a.climber_id < b.climber_id ? -1 : 1;
}

function climberTabulateRanks(climberData) {
  var lastEqual = climberData[0];
  lastEqual['rank'] = 1;

  // Display the same rank number for climbers with equal scores
  climberData.forEach(function(c, i) {
    if (c.tops === lastEqual.tops &&
        c.topAttempts === lastEqual.topAttempts &&
        c.bonuses === lastEqual.bonuses &&
        c.bonusAttempts === lastEqual.bonusAttempts) {
      c.rank = lastEqual.rank;
    } else {
      c.rank = i + 1;
      lastEqual = c;
    }
  });

  return climberData;
}