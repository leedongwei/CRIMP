import { Meteor } from 'meteor/meteor';
import { ValidatedMethod } from 'meteor/mdg:validated-method';

import { Messages } from '../imports/data/messages.js';



Meteor.startup(() => {
  // code to run on server at startup
});

var Api = new Restivus({
  useDefaultAuth: true,
  prettyJson: true
});

Api.addRoute('test', {
  get: function() {
    let msg = {
      'method': 'GET'
    };

    const insertStatus = Messages.methods.insert.call({
      "payload": msg
    });

    // simulate latency
    Meteor._sleepForMs(5000);

    return insertStatus;
  },
  post: function () {
    let msg = {
      'method': 'GET',
      'body': this.bodyParams,
    };

    const insertStatus = Messages.methods.insert.call({
      "payload": msg
    });

    return insertStatus;
  }
});
