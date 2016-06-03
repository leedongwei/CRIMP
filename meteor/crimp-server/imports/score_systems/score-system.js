/**
 *  Explanation and details of each Score System can be found above their
 *  class
 */
export const scoreSystemsNames = [
  'ifsc-top-bonus',
  'top-bonus-bonus',
  'top-flash-bonus2-bonus1',
  'points',
];

export class ScoreSystem {
  constructor(options) {
    this.name = options.name;
  }

  calculate(scoreString) {
    const calculatedScore = {};

    return calculatedScore;
  }

  tabulate(scoreArray) {
    const tabulatedScore = {};

    scoreArray.forEach((score) => {
      tabulatedScore.T.someField += score.calculatedScore.someField;
    });

    return tabulatedScore;
  }

  rankClimbers(climbersArray) {
    climbersArray.sort(this.rankFunction);
    return this.tabulateRanks(climbersArray);
  }

  rankFunction(climberA, climberB) {
    const a = climberA.tabulatedScore;
    const b = climberB.tabulatedScore;

    if (a.T !== b.T) {
      return a.T > b.T ? -1 : 1;
    }

    if (a.F !== b.F) {
      return a.F > b.F ? -1 : 1;
    }

    if (a.B !== b.B) {
      return a.B > b.B ? -1 : 1;
    }

    if (a.b !== b.b) {
      return a.b > b.b ? -1 : 1;
    }

    // if (a.scores_tiebreak !== b.scores_tiebreak) {
    //   return a.scores_tiebreak > b.scores_tiebreak ? -1 : 1;
    // }

    return 0;
  }

  tabulateRanks(climberArray) {
    try {
      let lastEqual = climberArray[0];
      lastEqual.rank = 1;

      // Display the same rank number for climbers with equal scores
      climberArray.forEach((c, i) => {
        if (this.rankFunction(c, lastEqual)) {
          c.rank = i + 1;
          lastEqual = c;
        } else {
          c.rank = lastEqual.rank;
        }
      });
    } catch (e) {
      // do nothing
    }

    return climberArray;
  }
}
