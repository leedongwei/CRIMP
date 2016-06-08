import { _ } from 'meteor/stevezhu:lodash';

import { ScoreSystem } from './score-system';


export default class IFSC_TB extends ScoreSystem {
  constructor() {
    super({
      name: 'IFSC Top-Bonus',
    });
  }

  calculate(scoreString) {
    const calculatedScore = {
      T: 0,
      T_attempts: 0,
      B: 0,
      B_attempts: 0,
      displayString: 'b0',
    };

    if (!scoreString.length) return calculatedScore;

    const targetString = _.toUpper(scoreString);
    _.forEach(targetString, (char, i) => {
      if (char === 'B' && !calculatedScore.B) {
        calculatedScore.B = 1;
        calculatedScore.B_attempts = i + 1;
      }

      if (char === 'T') {
        calculatedScore.T = 1;
        calculatedScore.T_attempts = i + 1;

        // If bonus is unset, set same value as T
        if (!calculatedScore.B) {
          calculatedScore.B = 1;
          calculatedScore.B_attempts = i + 1;
        }

        // Break for _.forEach
        return false;
      }

      return true;
    });


    if (calculatedScore.T) {
      calculatedScore.displayString = `t${calculatedScore.T_attempts} `
                                    + `b${calculatedScore.B_attempts}`;
    } else if (calculatedScore.B) {
      calculatedScore.displayString = `b${calculatedScore.B_attempts}`;
    }

    return calculatedScore;
  }

  tabulate(scoreArray) {
    const tabulatedScore = {
      T: 0,
      T_attempts: 0,
      B: 0,
      B_attempts: 0,
    };

    scoreArray.forEach((score) => {
      const calculatedScore = this.calculate(score.score_string);
      score.calculatedScore = calculatedScore;

      tabulatedScore.T += calculatedScore.T;
      tabulatedScore.T_attempts += calculatedScore.T_attempts;
      tabulatedScore.B += calculatedScore.B;
      tabulatedScore.B_attempts += calculatedScore.B_attempts;
    });

    return tabulatedScore;
  }

  rankFunction(climberA, climberB) {
    const a = climberA.tabulatedScore;
    const b = climberB.tabulatedScore;

    if (a.T !== b.T) return a.T > b.T ? -1 : 1;

    if (a.T_attempts !== b.T_attempts) return a.F < b.F ? -1 : 1;

    if (a.B !== b.B) return a.B > b.B ? -1 : 1;

    if (a.B_attempts !== b.B_attempts) return a.b < b.b ? -1 : 1;

    if (climberA.score_tiebreak !== climberB.score_tiebreak) {
      return climberA.score_tiebreak < climberB.score_tiebreak ? -1 : 1;
    }

    return 0;
  }
}
