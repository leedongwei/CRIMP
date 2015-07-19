// This takes in a .csv file and convert it into a Meteor command for the
// admin to paste into the browser console to initialize all climbers

// Check for filename in CLI
if (process.argv.length < 3) {
  console.log('To use this script: node ' + process.argv[1] + ' FILENAME.csv');
  process.exit(1);
}

// Read the file and print its contents.
var fs = require('fs'),
    filename = process.argv[2],
    climbers = [];

fs.readFile(filename, 'utf8', function(err, data) {
  if (err)  throw err;
  readFromFile(data);
});


function readFromFile(data) {
  var rows = data.split(/\r?\n/);

  for (var i=0; i < rows.length; i++) {
    var cells = rows[i].split(/,/),
        climber = {};

    climber['category_id'] = cells[0];
    climber['number'] = cells[1];
    climber['climber_id'] = cells[2];
    climber['climber_name'] = cells[3];
    climber['identity'] = cells[4];
    climber['affliation'] = cells[5];
    climbers.push(climber);
  }

  writeToFile(climbers);
}


function writeToFile(climbers) {
  var output = '';

  output += 'var climbers = ';
  output += JSON.stringify(climbers, null, 2);
  output += ';\r\n\r\n'
  output += 'for (var i = 0; i < climbers.length; i++) {\r\n'
  output += '  console.log(Meteor.call(\'addClimber\', climbers[i]));\r\n'
  output += '}\r\n';

  fs.writeFile("initialize_climbers_output.js", output, function(err) {
    if (err)  return console.error(err);
    console.log("Output was saved to initialize_climbers_output.js");
    console.log("Please log into CRIMP admin interface and paste the output into the browser console.");
  });
}