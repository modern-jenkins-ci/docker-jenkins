#!/bin/bash

if [ -e .seed.env ]; then
  . .seed.env
fi

LOAD_LOCAL_SECRETS=${LOAD_LOCAL_SECRETS:-true}
LOCAL_SECRETS=./config/secrets

if [ $LOAD_LOCAL_SECRETS == "true" ]; then
  if [ ! -d $LOCAL_SECRETS ]; then
    mkdir -p $LOCAL_SECRETS
  fi

  curl -sSL $SECRETS_SEED_URL -o $LOCAL_SECRETS/github

  if [ -e $LOCAL_SECRETS/github ]; then
    echo "Local Secrets exist...starting jenkins"
  else
    echo "Could not find local secrets, exiting..."
    exit 1
  fi
fi

if [ ! -d data/jenkins-slave ]; then
  mkdir -p data/jenkins-slave
fi

docker-compose up -d jenkins-slave
docker-compose logs -f jenkins-slave
