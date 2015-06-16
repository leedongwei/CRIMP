Router.route('/', function () {
  this.render('scoreboard');
});

Router.route('/admin', function () {
  this.render('admin');
});