RecentScores = new Mongo.Collection('recentscores');
CRIMP.schema.recentscore = new SimpleSchema({
  score_id: {
    label: 'ID of score-record',
    type: String
  },
  admin_id: {
    label: 'ID of admin',
    type: String,
    optional: true
  },
  climber_id: {
    label: 'ID of climber',
    type: String,
    min: 6,
    max: 6,
    denyUpdate: true
  },
  route_id: {
    label: 'ID of route',
    type: String,
    denyUpdate: true
  },
  score_string: {
    label: 'Raw scoring string',
    type: String,
    trim: false,
    optional: true
  },
  updated_at: {
    label: 'Update time of score-record',
    type: Date
  }
});


RecentScores.attachSchema(CRIMP.schema.recentscore);


Meteor.methods({
  /**
   *  Insert a score into recentscore stack. Maintains a maximum of 12
   *  documents in the stack.
   *
   *  @param
   *    {object} scoreDocument - A score document
   */
  insertRecentScore(scoreDocument) {
    CRIMP.checkPermission(CRIMP.roles.trusted);
    check(scoreDocument, Object);

    RecentScores.insert({
      'score_id': scoreDocument._id,
      'admin_id': scoreDocument.admin_id || '',
      'climber_id': scoreDocument.climber_id,
      'route_id': scoreDocument.route_id,
      'score_string': scoreDocument.score_string,
      'updated_at': scoreDocument.updated_at || ''
    }, (error, result) => {});

    while (result &&
           RecentScores.find({}).fetch().length > 12) {
      var oldestScore = findoldestScore();
      RecentScores.remove({ '_id': oldestScore._id }, (error, result) => {});
    }
  },

  /**
   *  Setup 12 entries for RecentScores on admin dashboard.
   *  Mainly used during UI/UX design, or to reset the entire board.
   */
  _initializeRecentScores() {
    CRIMP.checkPermission(CRIMP.roles.trusted);

    // Grab the 12 most recently updated documents
    var scores = Scores.find({}, { sort: { updated_at: -1 }, limit: 12 })
                       .fetch();

    // Delete everything in recentscores
    RecentScores.remove({}, (error, result) => {});

    // Add in the 12 documents
    for (let i=0; i < scores.length; i++) {
      Meteor.call('updateRecentScores', scores[i]);
    }
  },
})

function findoldestScore() {
  var oldestScore = null;
  var scores = RecentScores.find({}).fetch();

  for (var i=0; i < scores.length; i++) {
    if (!oldestScore) {
      oldestScore = scores[i];
      continue;
    }

    if (scores[i].updated_at < oldestScore.updated_at) {
      oldestScore = scores[i];
    }
  }

  return oldestScore;
}
