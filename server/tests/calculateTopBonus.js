var scores = ['1', 'B', 'T',
							'111B', '111B111', 'B111',
							'111T', '111T111', 'T111',
							'111B11T', '111T11B',
							'B111T','T111B',
							'BT111', 'TB111'];

scores.forEach(function(entry) {
	console.log(calculateTop(entry) + '/' + calculateBonus(entry));
});

function calculateTop (rawScore) {
	var i = 0;
	for (i; i < rawScore.length; i++) {
		if (rawScore[i] === 'T')
			return i+1;
	}
	return 0;
}


function calculateBonus (rawScore) {
	var i = 0;
	for (i; i < rawScore.length; i++) {
		if (rawScore[i] === 'T' || rawScore[i] === 'B')
			return i+1;
	}
	return 0;
}