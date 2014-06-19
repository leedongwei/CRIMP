# Comprehensive Realtime Index of Monkey Points (CRIMP)
###A realtime scoring and ranking system for [bouldering](http://http://en.wikipedia.org/wiki/Bouldering) competitions. Supports IFSC Top/Bonus scoring regulation.


## Interface
### Judges
/pictures
### Spectators
/pictures


## Demo
/video


## System Components
### admin
Set of command-line tools to:
1. Initialized participants data for use by all components
1. Periodically download and backup database onto local machine
1. Run analytics for post-event analysis on each route

Participants data is expected to be provided in an excel file by the admin/sales/whatever team. Local machine is expected to have PostgreSQL installed to store backups. Admin is expected to know what he/she is doing.

### client
Mobile-supported web application for spectators, with live ranking (maximum 5 sec delay). Participant data is to be pre-generated as a static page for SEO. Participant scores will be retrieved from `server`.

It is expected that users have Javascript-enabled browsers that support websockets. Otherwise, `client` will tell them to update their browsers.

### judge
Android app for judges to input scores. Emphasis placed on a simple and easy-to-use interface to reduce judges' workload. App uses QR code to identify climbers. It is assumed that judges will have a stable internet connection.

### paper
Generator for physical scoresheets with unique QR codes for every climber. Produces a single PDF file to send for printing. Would be nice to have a few minions to cut and sort them for distribution.

### server
node.js + express.js + einaros/ws server talking to a PostgreSQL database. Deployed on [Heroku](https://www.heroku.com/) free hobbyist plan, which is sufficient even for a week-long event. Architecture supports websockets on multiple Heroku web dynos.


## Technical Support & Development
Demand-based. Feel free to submit a pull-request, open an issue or send me an email :)


## Events/Users
* [NUS Climbing](http://www.nusclimb.com): NUS - Black Diamond [Boulderactive 2014](http://boulderactive.nusclimb.com)


## License
Developed by [DongWei](https://github.com/leedongwei) & [Weizhi](https://github.com/ecc-weizhi) for NUS - Black Diamond Boulderactive 2014