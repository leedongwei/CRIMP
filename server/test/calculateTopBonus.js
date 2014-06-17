var scores = ['1', 'B', 'T',
							'11B', '11B11', 'B11',
							'11T', '11T11', 'T11',
							'11BT', '11TB',
							'B11T', 'T11B',
							'BT11', 'TB11', ''];

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