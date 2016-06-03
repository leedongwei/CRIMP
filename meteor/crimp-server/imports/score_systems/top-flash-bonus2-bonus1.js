import { ScoreSystem } from './score-system';

export default class TFBb extends ScoreSystem {
  constructor() {
    super({
      name: 'top-flash-bonus2-bonus1',
    });
  }

  calculate(scoreString) {
    const calculatedScore = {
      T: 0,
      F: 0,
      B: 0,
      b: 0,
      string: '-',
    };

    if (!scoreString.length) return calculatedScore;

    if (scoreString.indexOf('T') === 0) {
      calculatedScore.T = 1;
      calculatedScore.F = 1;
      calculatedScore.string = 'F';
    } else if (scoreString.indexOf('T') >= 0) {
      calculatedScore.T = 1;
      calculatedScore.string = 'T';
    } else if (scoreString.indexOf('B') >= 0) {
      calculatedScore.B = 1;
      calculatedScore.string = 'B';
    } else if (scoreString.indexOf('b') >= 0) {
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
    };

    scoreArray.forEach((score) => {
      tabulatedScore.T += score.calculatedScore.T;
      tabulatedScore.F += score.calculatedScore.F;
      tabulatedScore.B += score.calculatedScore.B;
      tabulatedScore.b += score.calculatedScore.b;
    });

    return tabulatedScore;
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
}
