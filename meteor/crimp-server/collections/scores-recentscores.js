RecentScores = new Mongo.Collection('recentscores');
CRIMP.schema.recentscore = new SimpleSchema({
  score_id: {
    label: 'ID of score-record',
    type: String
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
    trim: false
  },
  updated_at: {
    label: 'Update time of score-record',
    type: Date
  }
});


RecentScores.attachSchema(CRIMP.schema.recentscore);

Meteor.methods({
  initRecentScores: function(data) {
    if (!Roles.userIsInRole(Meteor.user(), CRIMP.roles.trusted)) {
      throw new Meteor.Error(403, "Access denied");
    }

    var scores = Scores.find({}, { sort: { updated_at: -1 }, limit: 12 })
                  .fetch(),
        data = {};

    for (var i=0; i < scores.length; i++) {
      data['score_id'] = scores[i]._id;
      data['climber_id'] = scores[i].climber_id;
      data['route_id'] = scores[i].route_id;
      data['score_string'] = scores[i].score_string || '';
      data['updated_at'] = scores[i].updated_at;

      console.log(data)

      RecentScores.insert(data, function(error, insertedId) {
        if (error)  throw error;
      });
    }
  },
  updateRecentScores: function(data) {
    while (RecentScores.find({}).fetch().length > 12) {
      var oldestRecord = findOldestRecord();
      RecentScores.remove({ '_id': oldestRecord._id }, function(error, removedCount) {
        if (error)  throw error;
      });
    }

    RecentScores.insert({
      'score_id': data._id,
      'climber_id': data.climber_id,
      'route_id': data.route_id,
      'score_string': data.score_string,
      'updated_at': data.updated_at
    }, function(error, insertedId) {
      if (error)  throw error;
    });
  }
})

function findOldestRecord() {
  var oldestRecord = null,
      scores = RecentScores.find({}).fetch();

  for (var i=0; i < scores.length; i++) {
    if (!oldestRecord) {
      oldestRecord = scores[i];
      continue;
    }

    if (scores[i].updated_at < oldestRecord.updated_at) {
      oldestRecord = scores[i];
    }
  }

  return oldestRecord;
}