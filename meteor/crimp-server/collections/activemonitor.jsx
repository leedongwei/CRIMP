ActiveMonitor = new Mongo.Collection('activemonitor');
CRIMP.schema.activemonitor = new SimpleSchema({
  route_id: {
    label: 'ID of route',
    type: String,
    index: true,
    unique: true
  },
  admin_id: {
    label: 'ID of admin',
    type: String
  },
  admin_name: {
    label: 'Name of admin',
    type: String
  },
  admin_expiry: {
    label: 'Time to delete document',
    type: Date,
    autoValue() {
      // expires in 15mins
      return new Date(Date.now() + 10000);
    }
  },
  climber_id: {
    label: 'ID of climber',
    type: String,
    optional: true
  },
  climber_name: {
    label: 'Name of climber',
    type: String,
    optional: true
  },
  climber_expiry: {
    label: 'Time to remove climber',
    type: Date,
    optional: true,
    autoValue() {
      // expires in 10mins
      return new Date(Date.now() + 5000);
    }
  }
});


ActiveMonitor.attachSchema(CRIMP.schema.activemonitor);


// if (Meteor.isServer) {
//   Server-side methods at ../server/activemonitor.js
// }


// Future is used to return values from callbacks
if (Meteor.isServer)
  var Future = Meteor.npmRequire('fibers/future');

Meteor.methods({
  /**
   *  Insert a judge into ActiveMonitor. This creates a slot on ActiveMonitor
   *  for the route that the judge is on. It will expire in X minutes (see
   *  admin_expiry above) and be deleted.
   *  If there is an existing judge, he will be replaced with this new judge.
   *  If there is an existing climber, he will be wiped.
   *
   *  @param
   *    {object} selector - should contain: route_id
   *    {object} modifier - should contain: admin_id, admin_name
   *
   *  @return
   *    {number} - number of documents affected
   */
  insertActiveJudge(selector, modifier) {
    // Future is available server-side only
    if (!Meteor.isServer)   return;

    CRIMP.checkPermission(CRIMP.roles.trusted);
    check(selector, Object);
    check(modifier, Object);

    var fut = new Future();
    var sel, mod;

    if (('route_id' in selector) &&
        ('admin_id' in modifier) &&
        ('admin_name' in modifier)) {
      sel = {
        route_id: selector.route_id
      };

      mod = {
        admin_id: modifier.admin_id,
        admin_name: modifier.admin_name,
        climber_id: '',
        climber_name: '',
      };
    } else {
      throw new Meteor.Error(400, 'Bad syntax on arguments');
    }

    check(sel.route_id, String);
    check(mod.admin_id, String);
    check(mod.admin_name, String);

    ActiveMonitor.upsert(
      sel,
      { $set: mod },
      (error, result) => {
        if (error)
          fut.return(error);

        fut.return(result.numberAffected);
      }
    );

    return fut.wait();
  },


  /**
   *  Delete the slot from ActiveMonitor. It will do so even if there is a
   *  climber active on it
   *
   *  @param
   *    {object} selector - should contain route_id
   *
   *  @return
   *    {number} - number of documents affected
   */
  removeActiveJudge(selector) {
    // Future is available server-side only
    if (!Meteor.isServer)   return;

    CRIMP.checkPermission(CRIMP.roles.trusted);
    check(selector, Object);

    var fut = new Future();
    var sel;

    if ('route_id' in selector) {
      sel = {
        route_id: selector.route_id
      };

    } else {
      throw new Meteor.Error(400, 'Bad syntax on arguments');
    }

    check(sel.route_id, String);

    ActiveMonitor.remove(
      sel,
      (error, result) => {
        if (error)
          fut.return(error);

        fut.return(result);
      }
    );

    return fut.wait();
  },


  /**
   *  Essentially the same as insertActiveJudge, except that it allows climber
   *  data too. Inserts a slot if one does not exist.
   *  TODO: Refractor this.with insertActiveJudge.
   *
   *  @param
   *    {object} selector - should contain: route_id
   *    {object} modifier - should contain admin and climber data
   *
   *  @return
   *    {number} - number of documents affected
   */
  insertActiveClimber(selector, modifier) {
    // Future is available server-side only
    if (!Meteor.isServer)   return;

    CRIMP.checkPermission(CRIMP.roles.trusted);
    check(selector, Object);
    check(modifier, Object);

    var fut = new Future();
    var sel, mod;

    if (('route_id' in selector) &&
        ('admin_id' in modifier) &&
        ('admin_name' in modifier) &&
        ('climber_id' in modifier) &&
        ('climber_name' in modifier)) {
      sel = {
        route_id: selector.route_id
      };

      mod = {
        admin_id: modifier.admin_id,
        admin_name: modifier.admin_name,
        climber_id: modifier.climber_id,
        climber_name: modifier.climber_name,
      };
    } else {
      throw new Meteor.Error(400, 'Bad syntax on arguments');
    }

    check(sel.route_id, String);
    check(mod.admin_id, String);
    check(mod.admin_name, String);
    check(mod.climber_id, String);
    check(mod.climber_name, String);

    ActiveMonitor.upsert(
      sel,
      { $set: mod },
      (error, result) => {
        if (error)
          fut.return(error);

        fut.return(result.numberAffected);
      }
    );

    return fut.wait();
  },


  /**
   *  Removing climber data from the slot. Quite similar to removeActiveJudge
   *
   *  @param
   *    {object} selector - should contain route_id
   *
   *  @return
   *    {number} - number of documents affected
   */
  removeActiveClimber(selector) {
    // Future is available server-side only
    if (!Meteor.isServer)   return;

    CRIMP.checkPermission(CRIMP.roles.trusted);
    check(selector, Object);

    var fut = new Future();
    var sel, mod;

    if ('route_id' in selector) {
      sel = {
        route_id: selector.route_id
      };

      mod = {
        climber_id: '',
        climber_name: '',
      };
    } else {
      throw new Meteor.Error(400, 'Bad syntax on arguments');
    }

    check(sel.route_id, String);

    // Do not use upsert here
    ActiveMonitor.update(
      sel,
      { $set: mod },
      (error, result) => {
        if (error)
          fut.return(error);

        fut.return(result.numberAffected);
      }
    );

    return fut.wait();
  },


  /**
   *  Setup every route of a category on ActiveMonitor
   *
   *  @param
   *    {string} categoryId - category_id of any category
   */
  _initializeActiveMonitor(categoryId) {
    CRIMP.checkPermission(CRIMP.roles.trusted);
    check(categoryId, String);

    var category = Categories.findOne({category_id: categoryId});
    if (!category) {
      console.info(`category ${categoryId} does not exist`);
      return;
    }

    for (let i=1; i < category.route_count+1; i++) {
      Meteor.call('insertActiveJudge',
        { route_id: category.category_id + i.toString() },
        {
          admin_id: '0',
          admin_name: '_initializeActiveMonitor'
        }
      );
    }
  },


  /**
   *  Essentially _initializeActiveMonitor, but with random climbers.
   *  Primarily used for UI testing. It may throw an error if the number of
   *  climbers is less than the number of routes.
   *
   *  @param
   *    {string} categoryId - category_id of any category
   */
  _initializeActiveClimbers(categoryId) {
    CRIMP.checkPermission(CRIMP.roles.trusted);
    check(categoryId, String)

    var category = Categories.findOne({ category_id: categoryId });
    var climbers = Climbers.find({ category_id: categoryId }).fetch();

    if (!category || !climbers) {
      console.info(`category ${categoryId} does not exist`);
      return;
    }

    for (let i=0; i < category.route_count; i++) {
      Meteor.call('insertActiveClimber',
        {
          route_id: category.category_id + (i+1).toString()
        },
        {
          climber_id: climbers[i].climber_id,
          climber_name: climbers[i].climber_name,
          admin_id: '0',
          admin_name: '_initializeActiveMonitorExpanded'
        }
      );
    }
  }
});
