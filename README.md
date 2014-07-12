# Comprehensive Real-time Index of Monkey Points (CRIMP)
### A real-time scoring and ranking system for bouldering competitions/carnivals that uses the Top/Bonus scoring system.


## Interface
### Judges
/pictures to come
### Spectators
/pictures to come


## Demo
/video to come


## System Components
### web
Mobile-supported web application for spectators to display real-time information. There are 2 key features:

1. A list of climbers who are currently on the wall
2. Sorted and ranked list of climbers with their scores on each route

Participant data is pre-generated as a static page to help SEO. Spectators will connect to the `socket` to retrieve the latest scores. A persistent connection will be maintained so `socket` can push score updates to the spectators.


### judge
Android app for judges to submit scores. Emphasis placed on a simple and easy-to-use interface to reduce judges' workload. App uses QR code to identify climbers, but the keypad is available to manually enter the climber's ID as a backup. The app is fail-safe even if there is no internet connection - scores will be stored in a queue and sent out in sequence when connection is re-established.


### paper
Generator for physical scoresheets with unique QR codes for every climber. Produces a single PDF file to send for printing. It would be nice if you have a few minions to cut and sort them for distribution. Distributing the scoresheets in the call-zone will prevent climbers from losing them.

### backend
2 node.js applications deployed on [Heroku](https://www.heroku.com/) free hobbyist plan, which is more than sufficient for a week-long competition with 1000 climbers. Having 2 applications to separately handle judges and spectators ensures stability and security.

* `server`: Dedicated server for judges and admin to submit/review scores. Exposes a RESTful API that requires a password to write changes to database. Push updates to `socket` through a websocket connection.

* `socket`: Websocket server for `web` to retrieve latest scores. Can only read from database. Identifies updates from `server` and broadcasts them to all connected spectators.


### admin
Set of command-line tools to:

1. Initialized participants data for use by all components
1. Periodically download and backup database onto local machine
1. Analytics for post-event analysis on each route

Participants data is expected to be provided in an excel file by the admin/registration/sales/whatever team. Admin is expected to know what he/she is doing.



## Technical Support & Development
Demand-based. Feel free to submit a pull-request, open an issue or send us an email :)


## Users
* [NUS - Black Diamond Boulderactive 2014](http://boulderactive.nusclimb.com)
* [Asian University Climbing Championship 2014](http://nus.edu.sg/osa/src/competitive/competitions/aucc)


## License
Copyright © 2014, [DongWei](https://github.com/leedongwei) & [Weizhi](https://github.com/ecc-weizhi)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
