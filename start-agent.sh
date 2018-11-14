#!/bin/sh

if [ -e .jenkins.env ]; then
  . .jenkins.env
fi

if [ ! -d data/jenkins-agent ]; then
  mkdir -p data/jenkins-agent
fi

docker-compose up -d jenkins-agent
docker-compose logs -f jenkins-agent
