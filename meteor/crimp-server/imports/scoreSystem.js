/**
 *  Explanation and details of each Score System can be found above their
 *  class
 */
export const scoreSystemsNames = [
  'ifsc-top-bonus',
  'top-bonus-bonus',
  'carnival-points',
];

export class ScoreSystem {
  constructor(options) {
    this.name = options.name;
  }

  calculate(scoreObject) {
    const sstring = scoreObject.score_string;
    return sstring;
  }

  rankClimbers() {

  }

  rankFunction(a, b) {
    if (a.tops !== b.tops) {
      return a.tops > b.tops ? -1 : 1;
    }

    if (a.topAttempts !== b.topAttempts) {
      return a.topAttempts < b.topAttempts ? -1 : 1;
    }

    if (a.bonuses !== b.bonuses) {
      return a.bonuses > b.bonuses ? -1 : 1;
    }

    if (a.bonusAttempts !== b.bonusAttempts) {
      return a.bonusAttempts < b.bonusAttempts ? -1 : 1;
    }

    if (a.scores_tiebreak !== b.scores_tiebreak) {
      return a.scores_tiebreak > b.scores_tiebreak ? -1 : 1;
    }

    return a.climber_id < b.climber_id ? -1 : 1;
  }
}


