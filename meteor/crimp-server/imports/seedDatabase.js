import { Meteor } from 'meteor/meteor';
import { Random } from 'meteor/random';
import { faker } from 'meteor/practicalmeteor:faker';

import CRIMP from './settings';
import Events from './data/events';
import Categories from './data/categories';
import Teams from './data/teams';
import Climbers from './data/climbers';

// TODO: DISABLE FOR PRODUCTION
function seedDatabase() {
  // Do not allow this to run on production!
  if (CRIMP.ENVIRONMENT.NODE_ENV === 'production'
      || Meteor.isClient) return;


  /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
   *  Build 2 Events                                         *
   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
  const eve1 = Events.insert({
    event_name_full: faker.company.companyName(),
    event_name_short: 'Event 1',
    time_start: new Date(),
    time_end: new Date(),
  });

  // eve2 will be left empty
  const eve2 = Events.insert({
    event_name_full: faker.company.companyName(),
    event_name_short: 'Event 2',
    time_start: new Date(),
    time_end: new Date(),
  });


  /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
   *  Build 3 Categories                                     *
   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
  const cat1 = Categories.insert({
    category_name: faker.commerce.productName(),
    acronym: String(faker.random.uuid()).substr(0, 3),
    is_team_category: false,
    is_score_finalized: false,
    climber_count: 0,
    time_start: new Date(),
    time_end: new Date(),
    score_system: 'ifsc-top-bonus',
    routes: [{
      _id: Random.id(),
      route_name: 'R1',
      score_rules: {},
    }, {
      _id: Random.id(),
      route_name: 'R2',
      score_rules: {},
    }, {
      _id: Random.id(),
      route_name: 'R3',
      score_rules: {},
    }, {
      _id: Random.id(),
      route_name: 'R4',
      score_rules: {},
    }, {
      _id: Random.id(),
      route_name: 'R5',
      score_rules: {},
    }, {
      _id: Random.id(),
      route_name: 'R6',
      score_rules: {},
    }],
    event: {
      _id: eve1,
      event_name_full: 'eve1.event_name_full',
    },
  });

  const cat2 = Categories.insert({
    category_name: faker.commerce.productName(),
    acronym: String(faker.random.uuid()).substr(0, 3),
    is_team_category: false,
    is_score_finalized: false,
    climber_count: 0,
    time_start: new Date(),
    time_end: new Date(),
    score_system: 'points',
    routes: [{
      _id: Random.id(),
      route_name: 'R1',
      score_rules: {
        points: 10,
      },
    }, {
      _id: Random.id(),
      route_name: 'R2',
      score_rules: {
        points: 10,
      },
    }, {
      _id: Random.id(),
      route_name: 'R3',
      score_rules: {
        points: 10,
      },
    }, {
      _id: Random.id(),
      route_name: 'R4',
      score_rules: {
        points: 10,
      },
    }, {
      _id: Random.id(),
      route_name: 'R5',
      score_rules: {
        points: 10,
      },
    }, {
      _id: Random.id(),
      route_name: 'R6',
      score_rules: {
        points: 10,
      },
    }],
    event: {
      _id: eve1,
      event_name_full: 'eve1.event_name_full',
    },
  });

  const cat3 = Categories.insert({
    category_name: faker.commerce.productName(),
    acronym: 'TMS',
    is_team_category: true,
    is_score_finalized: false,
    climber_count: 0,
    time_start: new Date(),
    time_end: new Date(),
    score_system: 'points',
    routes: [{
      _id: Random.id(),
      route_name: 'TEAM R1',
      score_rules: {
        points: 10,
      },
    }, {
      _id: Random.id(),
      route_name: 'TEAM R2',
      score_rules: {
        points: 10,
      },
    }, {
      _id: Random.id(),
      route_name: 'TEAM R3',
      score_rules: {
        points: 10,
      },
    }, {
      _id: Random.id(),
      route_name: 'TEAM R4',
      score_rules: {
        points: 10,
      },
    }, {
      _id: Random.id(),
      route_name: 'TEAM R5',
      score_rules: {
        points: 10,
      },
    }, {
      _id: Random.id(),
      route_name: 'TEAM R6',
      score_rules: {
        points: 10,
      },
    }],
    event: {
      _id: eve1,
      event_name_full: 'eve1.event_name_full',
    },
  });


  /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
   *  Build 10 Climbers                                      *
   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
  function insertClimbers() {
    return Climbers.insert({
      climber_name: faker.name.findName(),
      identity: faker.phone.phoneNumberFormat(),
      affliation: faker.name.jobType(),
      categories: [],
    });
  }

  const climbers1 = [];
  for (let i = 0; i < 10; i++) {
    climbers1.push(insertClimbers());
  }

  const climbers2 = [];
  for (let i = 0; i < 10; i++) {
    climbers2.push(insertClimbers());
  }


  /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
   *  Add Climbers to cat1 and cat3                          *
   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

  climbers1.forEach((cmb) => {
    Categories.methods.addClimber.call({
      climberId: cmb,
      categoryId: cat1,
    });
  });

  climbers1.forEach((cmb) => {
    Categories.methods.addClimber.call({
      climberId: cmb,
      categoryId: cat3,
    });
  });

  climbers2.forEach((cmb) => {
    Categories.methods.addClimber.call({
      climberId: cmb,
      categoryId: cat2,
    });
  });

  /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
   *  Build 3 Teams for cat3                                 *
   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
  const parentCategory = Categories.findOne(cat3);
  const tms1 = Teams.methods.insert.call({
    parentCategory,
    teamName: faker.company.catchPhrase(),
  });

  const tms2 = Teams.methods.insert.call({
    parentCategory,
    teamName: faker.company.catchPhrase(),
  });

  const tms3 = Teams.methods.insert.call({
    parentCategory,
    teamName: faker.company.catchPhrase(),
  });

  /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
   *  Add 3 Climbers to each Team                            *
   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
  for (let i = 0; i < 3; i++) {
    Teams.methods.addClimber.call({
      climberId: climbers1[i],
      teamId: tms1,
    });
  }

  for (let i = 3; i < 6; i++) {
    Teams.methods.addClimber.call({
      climberId: climbers1[i],
      teamId: tms2,
    });
  }

  for (let i = 6; i < 9; i++) {
    Teams.methods.addClimber.call({
      climberId: climbers1[i],
      teamId: tms3,
    });
  }
}


Meteor.methods({
  seedDB: () => {
    seedDatabase();
  },
});


export default seedDatabase;
