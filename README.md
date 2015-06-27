# Comprehensive Real-time Index of Monkey Points (CRIMP)
### A real-time scoring and ranking system for climbing events


## Interface
### Spectators

### Judges

### Admin

## Scoring systems available
* IFSC Top/Bonus


## System Components
### web
Mobile-supported web application for spectators to display real-time information. There are 2 key features:

1. A list of climbers who are currently on the wall
2. Sorted and ranked list of climbers with their scores on each route


### judge
Android app for judges to submit scores. Emphasis placed on a simple and easy-to-use interface to reduce judges' workload. App uses QR code to identify climbers, but the keypad is available to manually enter the climber's ID as a backup. The app is fail-safe even if there is no internet connection - scores will be stored in a queue and sent out in sequence when connection is re-established.


### paper
Generator for physical scoresheets with unique QR codes for every climber. Produces a single PDF file to send for printing. It would be nice if you have a few minions to cut and sort them for distribution. Distributing the scoresheets in the call-zone will prevent climbers from losing them.


## Technical Support & Development
* Currently ongoing!
* [Git branching model](http://nvie.com/posts/a-successful-git-branching-model/)



## Users
* [NUS - Black Diamond Boulderactive 2014](http://boulderactive.nusclimb.com)
* [Asian University Climbing Championship 2014](http://nus.edu.sg/osa/src/competitive/competitions/aucc)


## License
Copyright © 2014-2015, [DongWei](https://github.com/leedongwei) & [Weizhi](https://github.com/ecc-weizhi)

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
