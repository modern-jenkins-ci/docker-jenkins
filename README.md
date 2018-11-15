# Jenkins+Docker

A _Dockerized_ Jenkins repository. You can use this repository as a basic reference for getting up and running with Jenkins quickly. The repository uses [docker-compose](https://docs.docker.com/compose/overview/) to start up a mostly configured Jenkins master along with a Jenkins agent. There are a few Jenkins init scripts to bypass the traditional configuration wizard and allows you to pre-configure:

* Admin credentials
* Build agent
* GitHub Integration
* GitHub Organization Project
* Proxy Settings
* Misc Jenkins best practices

## Playground

You can play with this pre-configured environment by going to this link:

[https://www.katacoda.com/modern-jenkins/scenarios/jenkins-docker-configured](https://www.katacoda.com/modern-jenkins/scenarios/jenkins-docker-configured)

## Configuration

The following three configuration scenarios are available. You will need to choose one.

### Scenario 1: Basic Configuration

If you want a basic level of pre-configuration you can run with the following exported environment variables before you start up:

```Script
export JAVA_OPTS=-Djenkins.install.runSetupWizard=false
export JENKINS_MASTER_EXECUTORS=0
export JENKINS_AGENT_EXECUTORS=2
export LOCAL_ADMIN=admin
export LOCAL_PASSWORD=admin
export JENKINS_FRONTEND_URL=http://<your-hostname-here>:8080
export JENKINS_SERVER=jenkins-master
```

### Scenario 2: Advanced Configuration

If you want the full level of pre-configuration, and want to take advantage of all the Jenkins initialization scripts you will need to configure a `jenkins.env` environment file at the root of the project. A sample file is provided with documentation for each environment variable. The sample file can be found here: [jenkins-env-sample.env](./jenkins-env-sample.env).

## Getting Started

Once you chose your configuration options, you can start the Jenkins master and build agent docker-compose with the following command.

### Running Behind a Proxy

If you are running your Jenkins infrastructure behind a proxy, you will want to make sure you have exported the following environment variables prior to starting up the containers:

```Script
export http_proxy=http://your-proxy-server:port
export https_proxy=http://your-proxy-server:port
export no_proxy=localhost<, other servers here>
```

### Scenario 1

For scenario 1, you will need to make sure you have exported your environment variables before starting the docker containers. This can be wrapped up into a script if needed.

Example:

```Script
export ...
docker-compose up -d
```

This will start up the Jenkins master and build agent containers in the background. If you want to see the logs you can run:

```Script
# All logs:
docker-compose logs -f

# Master logs:
docker-compose logs -f jenkins-master

# Agent logs:
docker-compose logs -f jenkins-agent
```

> NOTE: The build agent will not start on the first run, see [First Run Considerations](#first-run-considerations)

### Scenario 2

For scenario 2 there are specific scripts to start the Jenkins master and build agent. The logs are automatically tailed when running these scripts.

```Script
#start the master server
./start-master.sh

# start the build agent
./start-agent.sh
```

### First Run Considerations

For all scenario's on the first run the build agent container will attempt to start but will fail. This is expected behavior. The build agent requires a secret which can only be obtained after it is generated after the first run. The build agent join secret is an environment variable which will need to be exported after you acquire it from the Jenkins user interface. After the Master starts and is configured, navigate to `$JENKINS_FRONTEND_URL/computer/docker-agent` and grab the 65 character secret on the user interface. Example from the user interface:

```
* Run from agent command line:

java -jar agent.jar -jnlpUrl http://localhost:8080/computer/docker-agent/slave-agent.jnlp -secret 3d1b911570fe8e55c0296b7fb3b7e68ab12ac00793eb310d137570460833bd90 

--------------------------------------------------------------------------------------------------^^^ this is what you want to grab, everything after the -secret flag.
```

Then export:

```Script
export JOIN_SECRET=3d1b911570fe8e55c0296b7fb3b7e68ab12ac00793eb310d137570460833bd90

# then restart the agent
docker-compose up -d jenkins-agent
```

## Usage

Once Jenkins is up and running, and you have configured the Jenkins agent, you should be able to start creating Jobs. You can do further configurations at this point.


## Disclaimer

This repository was created for demonstration purposes and is not intended for production use. It is meant as a way to get familiar with Jenkins and requires further customizations to be production ready. Use at your own risk.


