# Used to create a jenkins build agent with embedded software
FROM jenkins/slave:alpine

COPY --from=docker:latest /usr/local/bin/docker /usr/local/bin/docker

USER root

# Example of adding extra tools
# RUN apt-get update && apt-get install -y jq
RUN apk add --update --no-cache jq

USER jenkins