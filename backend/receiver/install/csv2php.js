var fs = require('fs');
var page_category = 'AWF';
var	header = '<?php\r\n' +
			'$category=\''+page_category+'\';	//define category\r\n' +
			'date_default_timezone_set(\'Asia/Singapore\');\r\n' +
			'$date = getdate();\r\n' +
			'include(\'includes/header.php\');\r\n' +
			'include(\'includes/activeClimber.php\')\r\n' +
			'?>\r\n\r\n',
		footer = '<?php include(\'includes/footer.php\'); ?>',
		divstring = '<div class="climber clearfix" id="rank%s">' +
			'<div id="%s">' +
			'<span class="c_rank">%s</span>' +
			'<span class="c_id">%s</span>' +
			'<h3 class="c_name">%s</h3>' +
			'<span class="c_top badge"><span class="top">0</span>t<span class="topAttempts">0</span></span>' +
			'<span class="c_bonus badge"><span class="bonus">0</span>b<span class="bonusAttempts">0</span></span>' +
			'<div class="c_score">' +
				'<div class="c_score-col" id="route1">t<span class="top">0</span> b<span class="bonus">0</span></div>' +
				'<div class="c_score-col" id="route2">t<span class="top">0</span> b<span class="bonus">0</span></div>' +
				'<div class="c_score-col" id="route3">t<span class="top">0</span> b<span class="bonus">0</span></div>' +
				'<div class="c_score-col" id="route4">t<span class="top">0</span> b<span class="bonus">0</span></div>' +
				'<div class="c_score-col" id="route5">t<span class="top">0</span> b<span class="bonus">0</span></div>' +
				'<div class="c_score-col" id="route6">t<span class="top">0</span> b<span class="bonus">0</span></div>' +
				'</div></div></div>\r\n\r\n',
	insertData = '<section class="climberScores container">' +
			'<div class="scoreHeader clearfix">' +
				'<h1>****</h1>' +
				'<h2>Live Scores</h2>' +
				'<span class="c_top">Top</span>' +
				'<span class="c_bonus">Bonus</span>' +
			'</div>\r\n\r\n';

function parse(str) {
  var args = [].slice.call(arguments, 1),
    i = 0;

  return str.replace(/%s/g, function() {
    return args[i++];
  });
}

var csvFile = './participantsPHP.csv';
fs.readFile(csvFile, 'utf8', function (err, data) {
	if (err) {
    return console.log(err);
  }

  var i = 0,
  		rows, cells;

  data = data.replace(/\r\n/g, '@@@').replace(/[\r\n]/g, '@@@');
  rows = data.split('@@@');
  var i = 1;
  rows.forEach(function (entry) {
  	if (entry && entry != ',') {
  		console.log(JSON.stringify(entry));
	  	cells = entry.split(',');
	  	insertData += parse(divstring, i, cells[0], i, cells[0], cells[1]);
	  	i++
  	} else {
  		insertData += '</section>';
  	}
  });

  //console.log (insertData);
  fs.writeFile(page_category+'.php', header + insertData + footer, function (err) {
		  if (err) throw err;
		  console.log('It\'s saved!');
		});
});
