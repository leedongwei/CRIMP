import { _ } from 'meteor/stevezhu:lodash';

/**
 *  Explanation and details of each Score System can be found above their
 *  class
 */
export const scoreSystemsNames = [
  'ifsc-top-bonus',
  'top-bonus-bonus',
  'top-bonus1-bonus2',
  'top-flash-bonus2-bonus1',
  'points',
];

export class ScoreSystem {
  constructor(options) {
    this.name = options.name;
  }

  /**   Expected custom method for child-class of ScoreSystem
   *  Takes in Scores.scores.$.score_string from a route in the Score document
   *  and calculate the results of that route, returning a Object unique to
   *  the implementation of Score System
   *
   *  @param {string} scoreString
   *  @return {Object} calculatedScore
   *  @return {string} calculatedScore.displayString
   */
  calculate(scoreString) {
    const calculatedScore = {
      A: 0,
      B: 0,
      displayString: scoreString,
    };

    return calculatedScore;
  }

  /**   Expected custom method for child-class of ScoreSystem
   *  Takes in Scores.scores array from Score document and combine the
   *  calculated scores from all the routes
   *
   *  @param {Object[]} scoreArray
   *  @return {Object}  tabulatedScore
   */
  tabulate(scoreArray) {
    const tabulatedScore = {
      A: 0,
      B: 0,
    };

    scoreArray.forEach((score) => {
      const calculatedScore = this.calculate(score.score_string);
      score.calculatedScore = calculatedScore;

      tabulatedScore.A += calculatedScore.A;
      tabulatedScore.B += calculatedScore.B;
    });

    return tabulatedScore;
  }

  /**   Expected custom method for child-class of ScoreSystem
   *  Comparator function for climbers based on their tabulatedScores
   *
   *  @param {Object} climber
   *  @param {Object} climber.tabulatedScore
   *  @return {number} -1 or 0 or 1
   */
  rankFunction(climberA, climberB) {
    const a = climberA.tabulatedScore;
    const b = climberB.tabulatedScore;

    if (a.A !== b.A) {
      return a.T > b.T ? -1 : 1;
    }

    if (a.B !== b.B) {
      return a.F > b.F ? -1 : 1;
    }

    if (climberA.score_tiebreak !== climberB.score_tiebreak) {
      return climberA.score_tiebreak < climberB.score_tiebreak ? -1 : 1;
    }

    return 0;
  }

  /**
   *  Takes in an array of Climbers documents joint with Scores and assign
   *  a rank to each of the climbers
   *
   *  @param {Object[]} climbersArray
   *  @param {Object} climbersArray.$ (see jointDoc)
   *  @param {Object} climbersArray.$.tabulatedScore
   *  @return {Object[]} climbersArray
   */
  rankClimbers(climbersArray) {
    climbersArray.sort(this.rankFunction);
    return this.tabulateRanks(climbersArray);
  }

  /**
   *  Give a rank to the climbers based on their position in an sorted array
   *  and account for climbers having the same score.
   *
   *  @param {Object[]} climberArray
   *  @return {Object[]} climberArray
   *  @return {number} climberArray.$.rank
   */
  tabulateRanks(sortedClimberArray) {
    if (!sortedClimberArray.length) return sortedClimberArray;

    let lastEqual = sortedClimberArray[0];
    lastEqual.rank = 1;

    // Assign the same rank for climbers with equal scores
    sortedClimberArray.forEach((climber, index) => {
      if (this.rankFunction(climber, lastEqual)) {
        climber.rank = index + 1;
        lastEqual = climber;
      } else {
        climber.rank = lastEqual.rank;
      }
    });

    return sortedClimberArray;
  }

  /**
   *  Standard method to join a Climber document and a Score document
   *
   *  @param {Object} climberDoc
   *  @param {Object} scoreDoc
   *  @return {Object} jointDoc
   */
  join(climberDoc, scoreDoc) {
    const jointDoc = climberDoc;
    const categoryData = _.find(jointDoc.categories,
                                (c) => c._id === scoreDoc.category_id);

    jointDoc.scores = scoreDoc.scores;
    jointDoc.marker_id = scoreDoc.marker_id;
    jointDoc.score_tiebreak = categoryData.score_tiebreak;

    return jointDoc;
  }
}
