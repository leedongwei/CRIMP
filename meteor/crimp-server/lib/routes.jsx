/**
 *  Router will append the template at the bottom of index.html
 */

Router.route('/', function() {
  this.render('crimp_spectator');
});

Router.route('/admin', function() {
  this.render('crimp_admin');
});
