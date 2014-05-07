# Realtime Ranking System for Bouldering Competitions

Made to provide live scores for NUS-BD Boulderactive 2014

## Components
###admin
Admin interface with CRUD functions to competitors, rounds, routes and competition data. Mainly used to review scores at the end of the round and confirm accuracy of data.

###client
Web app for spectators.

###judge
Android/web app for judges to input scores. It uses QR code to identify climbers.

###paper
Generator for physical scoresheets with unique QR codes for every climber.

###server
node.js server to take in scores and push them to spectators
