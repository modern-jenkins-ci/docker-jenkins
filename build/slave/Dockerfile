# Used to create a jenkins slave agent with embedded software
FROM jenkins/slave:alpine

COPY --from=docker:latest /usr/local/bin/docker /usr/local/bin/docker

USER root

RUN apt-get update && apt-get install -y jq

