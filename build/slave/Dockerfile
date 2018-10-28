# Used to create a jenkins slave agent with embedded software
FROM jenkins/slave:alpine

COPY --from=docker:latest /usr/local/bin/docker /usr/local/bin/docker

USER root

# Example of adding extra tools
RUN apk add --update --no-cache jq

USER jenkins