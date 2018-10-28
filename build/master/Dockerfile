FROM jenkins/jenkins:lts

LABEL MAINTAINER "Ernesto Ojeda <ernesto.ojeda@intel.com>"

USER root

# Install plugins
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

COPY init.groovy.d/* /usr/share/jenkins/ref/init.groovy.d/
RUN chown -R jenkins:jenkins /usr/share/jenkins/ref

USER jenkins

ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"

# this comes from base image jenkins/jenkins:lts
ENTRYPOINT /usr/local/bin/jenkins.sh