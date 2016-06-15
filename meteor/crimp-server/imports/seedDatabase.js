import { Meteor } from 'meteor/meteor';
import { Random } from 'meteor/random';
import { faker } from 'meteor/practicalmeteor:faker';
import { _ } from 'meteor/stevezhu:lodash';

import CRIMP from './settings';
import Events from './data/events';
import Categories from './data/categories';
import Teams from './data/teams';
import Climbers from './data/climbers';
import Scores from './data/scores';


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
    score_system: 'IFSC-Top-Bonus',
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
    },{
      _id: Random.id(),
      route_name: 'R7',
      score_rules: {},
    },{
      _id: Random.id(),
      route_name: 'R8',
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
    score_system: 'TFBb',
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

  const cat3 = Categories.insert({
    category_name: faker.commerce.productName(),
    acronym: 'TMS',
    is_team_category: true,
    is_score_finalized: false,
    climber_count: 0,
    time_start: new Date(),
    time_end: new Date(),
    // score_system: 'Points',
    score_system: 'IFSC-Top-Bonus',
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
      gender: faker.random.boolean() ? 'M' : 'F',
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












function mockComp() {
  /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
   *  Build 1 Events                                         *
   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
  const eve1 = Events.insert({
    event_name_full: 'Boulderactive',
    event_name_short: 'Event 1',
    time_start: new Date(),
    time_end: new Date(),
  });

  /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
   *  Build 1 Categories                                     *
   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
  const cat1 = Categories.insert({
    category_name: 'Mock Comp',
    acronym: 'MOC',
    is_team_category: false,
    is_score_finalized: false,
    climber_count: 0,
    time_start: new Date(),
    time_end: new Date(),
    score_system: 'TFBb',
    routes: [{
      _id: Random.id(),
      route_name: 'Route 1',
      score_rules: {},
    }, {
      _id: Random.id(),
      route_name: 'Route 2',
      score_rules: {},
    }, {
      _id: Random.id(),
      route_name: 'Route 3',
      score_rules: {},
    }, {
      _id: Random.id(),
      route_name: 'Route 4',
      score_rules: {},
    }, {
      _id: Random.id(),
      route_name: 'Route 5',
      score_rules: {},
    }, {
      _id: Random.id(),
      route_name: 'Route 6',
      score_rules: {},
    }, {
      _id: Random.id(),
      route_name: 'Route 7',
      score_rules: {},
    }, {
      _id: Random.id(),
      route_name: 'Route 8',
      score_rules: {},
    }],
    event: {
      _id: eve1,
      event_name_full: 'eve1.event_name_full',
    },
  });


  /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
   *  Build Climbers                                      *
   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
  function insertClimbers(name) {
    return Climbers.insert({
      climber_name: name,
      identity: faker.phone.phoneNumberFormat(),
      affliation: 'NUS Climbing',
      categories: [],
    });
  }

  const names = [
    'Pat',
    'Day',
    'Alichow',
    'Ange',
    'Jas',
    'Lim',
    'Fern',
    'Gwen',
    'Jovin',
    'Sab',
    'Juan',
    'Dave',
    'Josh Tan',
    'Leon',
    'Jon',
    'Josh Ko',
    'Yukai',
    'ZwJ',
    'Jingyang',
    'Jem Moi',
    'Saw Jhen Jin',
    'Ong Wei Yang',
    'Theodor Gunawan',
    'Chinab',
    'Tricia Yau',
    'Nalleong Huikkataivalyu',
    'Ryofred',
    'Zhimin',
    'Azli',
  ];

  const climbers1 = [];
  names.forEach((n) => {
    climbers1.push(insertClimbers(n));
  });


  /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
   *  Add Climbers to cat1 and cat3                          *
   * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
  climbers1.forEach((cmb) => {
    Categories.methods.addClimber.call({
      climberId: cmb,
      categoryId: cat1,
    });
  });
}


Meteor.methods({
  seedDB: () => {
    seedDatabase();
  },

  mockComp: () => {
    mockComp();
  },

  // TODO: Remove after mock comp
  deleteAll: () => {
    CRIMP.checkRoles(CRIMP.roles.admins, this.userId);

    Scores.remove({});
    Climbers.remove({});
    Teams.remove({});
    Categories.remove({}, null, true);
    Events.remove({}, null, true);
  },

  insertClimbers: ({climberArray, categoryId, gender}) => {
    _.forEach(climberArray, (c) => {
      const climberDoc = {
        gender,
        climber_name: c,
        identity: '',
        affliation: '',
        categories: [],
      };

      Climbers.insert(climberDoc);
    });
  },

  insertCategory: (categoryDoc) => {
    return Categories.insert(categoryDoc);
  },

  insertEvent: (eventDoc) => {
    return Events.insert(eventDoc);
  },
});


export default seedDatabase;
