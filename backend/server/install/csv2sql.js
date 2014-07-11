var fs = require('fs'),
		dropTable = 'DROP TABLE IF EXISTS crimp_data CASCADE;\r\n',
		createTable = 'CREATE TABLE crimp_data (c_id VARCHAR(5) PRIMARY KEY, c_name TEXT NOT NULL, c_category VARCHAR(3) NOT NULL, q01_judge TEXT, q01_raw TEXT, q01_top SMALLINT, q01_bonus SMALLINT, q02_judge TEXT, q02_raw TEXT, q02_top SMALLINT, q02_bonus SMALLINT, q03_judge TEXT, q03_raw TEXT, q03_top SMALLINT, q03_bonus SMALLINT, q04_judge TEXT, q04_raw TEXT, q04_top SMALLINT, q04_bonus SMALLINT, q05_judge TEXT, q05_raw TEXT, q05_top SMALLINT, q05_bonus SMALLINT, q06_judge TEXT, q06_raw TEXT, q06_top SMALLINT, q06_bonus SMALLINT, f01_judge TEXT, f01_raw TEXT, f01_top SMALLINT, f01_bonus SMALLINT, f02_judge TEXT, f02_raw TEXT, f02_top SMALLINT, f02_bonus SMALLINT, f03_judge TEXT, f03_raw TEXT, f03_top SMALLINT, f03_bonus SMALLINT, f04_judge TEXT, f04_raw TEXT, f04_top SMALLINT, f04_bonus SMALLINT);\r\n',
		insertData = 'INSERT INTO crimp_data (c_id, c_name, c_category) VALUES '

var csvFile = './participantsSQL.csv';
fs.readFile(csvFile, 'utf8', function (err, data) {
	if (err) {
    return console.log(err);
  }

  var isHeader = true,
  		i = 0,
  		rows, cells;

  data = data.replace(/\r\n/g, '@@@').replace(/[\r\n]/g, '@@@');
  rows = data.split('@@@');
  rows.forEach(function (entry) {
  	if (isHeader) {
  		isHeader = false;
  	} else if (i > 98) {
  		// command line has a max-length, so we'll split it into several lines
  		// 100 is just an arbitrary number that seems to work
  		insertData = insertData.substring(0, insertData.length - 1);
  		insertData += ';\r\n\r\n' +
  			'INSERT INTO crimp_data (c_id, c_name, c_category) VALUES ';

  		insertData += '(\'';
	  	insertData += entry.replace(/,/g, '\',\'');
	  	insertData += '\'),';
  		i = 0
  	} else if(entry) {
	  	insertData += '(\'';
	  	insertData += entry.replace(/,/g, '\',\'');
	  	insertData += '\'),';
  		i++;
  	} else {
  		// replace the last comma with a semi-colon
  		insertData = insertData.substring(0, insertData.length - 1);
  		insertData += ';';
  	}
  });

  //console.log (insertData);
  fs.writeFile('csv2sql-output-schema.sql', dropTable + createTable + insertData, function (err) {
		  if (err) throw err;
		  console.log('It\'s saved!');
		});
});
