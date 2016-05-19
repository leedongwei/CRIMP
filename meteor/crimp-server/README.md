# crimp-server documentation
This is a draft document that I will brain-dump into as I work on the meteor app. It's will be messy. I'm not going to make sense because I'm probably half-asleep.


## Explanation of folder structure and load order
* http://guide.meteor.com/structure.html#load-order


## Deploying to a Docker container
* Build a meteor bundle `meteor build --architecture=os.linux.x86_64 ../../../crimp-build`
* Open Docker daemon, cd to meteor directory
* `docker run -d \
    -e ROOT_URL=http://crimp-dev.com \
    -e MONGO_URL=mongodb://url \
    -e MONGO_OPLOG_URL=mongodb://oplog_url \
    -v /Users/dongwei/Documents/Projects/meteor-build:/bundle \
    -p 8080:80 \
    meteorhacks/meteord:base`


## Build local Docker image locally
* Start `Docker Terminal`
* Make sure you have a MongoDB container running
  * Check on Kitematic
* `cd meteor\crimp-server`
* `docker build -t leedongwei/crimp-dev .`
* `docker run -d \
    -e ROOT_URL=http://localhost \
    -e MONGO_URL=mongodb://192.168.99.100:32769 \
    -p 8080:80 \
    leedongwei/crimp-dev`
* Go to Kitematic to check if it is running correct
* Git push to repo to trigger automated build on Docker hub



## Deploy using Docker + AWS
* [Link Docker Cloud to AWS](https://docs.docker.com/docker-cloud/getting-started/link-aws/)
* [Push local image to Docker Hub](https://docs.docker.com/mac/step_six/)


## Delete local Docker stuff
* Containers: `docker rm $(docker ps -a -q)`
* Images: `docker rmi $(docker images -q)`


## Set custom URL
* Find the IP Address of your AWS server
* Find the port of your Docker container
* Go to the CNAME records of your domain
* Add a URL-redirect

## Run tests
* `meteor test --driver-package=practicalmeteor:mocha --port=5000`
