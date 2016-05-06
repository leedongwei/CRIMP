import { Meteor } from 'meteor/meteor';
import { Restivus } from 'meteor/nimble:restivus';

import { Messages } from '../imports/data/messages.js';


Meteor.startup(() => {
  // code to run on server at startup
});

const Api = new Restivus({
  useDefaultAuth: true,
  prettyJson: true,
});

Api.addRoute('test', {
  get: () => {
    const msg = {
      method: 'GET',
    };

    const insertStatus = Messages.methods.insert.call({
      payload: msg,
    });

    // simulate latency
    Meteor._sleepForMs(5000);

    return insertStatus;
  },
  post: () => {
    const msg = {
      method: 'GET',
      body: this.bodyParams,
    };

    const insertStatus = Messages.methods.insert.call({
      payload: msg,
    });

    return insertStatus;
  },
});
