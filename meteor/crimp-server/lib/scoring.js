/* * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  Keep logic functions pertaining to scoring here
  Model functions can be found in collections/scores.js
  View functions can be found in client/views/templates/scoreboard.js

  Hopefully, this makes it easier for to implement other scoring
  systems in the future

 * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
CRIMP.scoring = {
  calculateTop: function(scoreString) {
    for (var i=0; i < scoreString.length; i++) {
      scoreString = scoreString.toUpperCase();
      if (scoreString[i] === 'T') {
        return i+1;
      }
    }
    return 0;
  },

  calculateBonus: function(scoreString) {
    for (var i=0; i < scoreString.length; i++) {
      scoreString = scoreString.toUpperCase();
      if (scoreString[i] === 'T' || scoreString[i] === 'B') {
        return i+1;
      }
    }
    return 0;
  }
}