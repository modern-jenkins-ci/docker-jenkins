#!/bin/bash

if [ -e .seed.env ]; then
  . .seed.env
fi

LOCAL_SECRETS=./config/secrets

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

docker-compose up -d
docker-compose logs -f jenkins-master
