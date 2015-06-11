/* * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  Keep logic functions pertaining to scoring here
  Model functions can be found in collections/scores.js
  View functions can be found in client/views/templates/scoreboard.js

  Hopefully, this makes it easier for to implement other scoring
  systems in the future

 * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

function calculateTop(scoreString) {
  for (var i=0; i < scoreString.length; i++) {
    if (scoreString[i] === 'T')
      return i+1;
  }
  return 0;
}

function calculateBonus(scoreString) {
  for (var i=0; i < scoreString.length; i++) {
    if (rawScore[i] === 'T' || rawScore[i] === 'B')
      return i+1;
  }
  return 0;
}