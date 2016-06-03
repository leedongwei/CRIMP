import ScoreSystem from '../scoreSystem.js';

export default class TFBb {
  constructor(name) {
    this.name = name;
  }
  calculate(scoreObject) {
    const rawScore = scoreObject.score_string;
    const calculatedScore = {
      T: 0,
      F: 0,
      B: 0,
      b: 0,
      string: '-',
    }

    if (!rawScore.length) return calculatedScore;

    if (rawScore.indexOf('T') == 0) {
      calculatedScore.T = 1;
      calculatedScore.F = 1;
      calculatedScore.string = 'F';
    } else if (rawScore.indexOf('T') >= 0) {
      calculatedScore.T = 1;
      calculatedScore.string = 'T';
    } else if (rawScore.indexOf('B') >= 0) {
      calculatedScore.B = 1;
      calculatedScore.string = 'B';
    } else if (rawScore.indexOf('b') >= 0) {
      calculatedScore.b = 1;
      calculatedScore.string = 'b';
    }

    return calculatedScore;
  }

  tabulate(scoreArray) {
    const tabulatedScore = {
      T: 0,
      F: 0,
      B: 0,
      b: 0,
    }
    scoreArray.forEach((score) => {
      tabulatedScore.T += score.calculatedScore.T;
      tabulatedScore.F += score.calculatedScore.F;
      tabulatedScore.B += score.calculatedScore.B;
      tabulatedScore.b += score.calculatedScore.b;
    });

    return tabulatedScore;
  }

  rankClimbers(climbersArray) {
    climbersArray.sort(this.rankFunction);

    climbersArray.forEach((c, i) => {
      c.rank = i+1;
    });

    return climbersArray
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

    return a.marker_id < b.marker_id ? -1 : 1;
  }
}


