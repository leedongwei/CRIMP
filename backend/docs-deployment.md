//TODO
WEBSOCKETS `heroku labs:enable websockets -a APP-NAME`
SHARE POSTGRES `heroku config:add DATABASE_URL=postgres://address -a crimp-socket`

#Deployment on Heroku
1. Get a free [Heroku](http://www.heroku.com) Hobbyist plan
1. Create an app and name it from the dashboard
1. Follow instructions here: https://devcenter.heroku.com/articles/getting-started-with-nodejs
	* `git init` inside 'server' folder
	* To add the contents of 'server' into the app that you've created earlier, use `git remote add heroku git@heroku.com:APP-NAME.git`
	* Deploy with `git push heroku master`
	* Test by sending a GET request using your browser `http://APP-NAME.herokuapp.com/judges/get/:round`, you should receive see the JSON reply


