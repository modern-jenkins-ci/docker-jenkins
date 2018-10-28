#!/bin/bash

if [ -e .seed.env ];
  . .seed.env
fi

LOCAL_SECRETS=./config/secrets

wget $SECRETS_SEED_URL \
  -O $LOCAL_SECRETS/github

if [ -e $LOCAL_SECRETS/github ]; then
  echo "Local Secrets exist...starting jenkins"
else
  echo "Could not find local secrets, exiting..."
  exit 1
fi

docker-compose up -d
docker-compose logs -f jenkins-master
