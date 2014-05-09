# Comprehensive Realtime Index of Monkey Points (CRIMP)
###A Realtime Ranking System for Bouldering Competitions
Made to for [NUS - Black Diamond Boulderactive 2014](http://boulderactive.nusclimb.com/).

## System Components
###admin
Admin interface with CRUD functions to competitors, rounds, routes and competition data. Mainly used to cross-check with physical scoresheets at the end of the round to confirm accuracy of data. Includes functions to generate statistics of routes.

###client
Web app for spectators with live ranking.

###judge
Android app for judges to input scores. Emphasis placed on a simple and easy-to-use interface to reduce judges workload. App uses QR code to identify climbers.

###paper
Generator for physical scoresheets with unique QR codes for every climber.

###server
node.js server to take in scores, store into a database and push them to spectators.