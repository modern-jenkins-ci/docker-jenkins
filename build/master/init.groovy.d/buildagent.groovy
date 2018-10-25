#!groovy

import jenkins.model.*
import hudson.model.*
import hudson.slaves.*
import hudson.plugins.sshslaves.*
import java.util.ArrayList;
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry;

def createSlave(instance, name, label, numExecutors) {
  List<Entry> agentEnv = new ArrayList<Entry>();
  EnvironmentVariablesNodeProperty envPro = new EnvironmentVariablesNodeProperty(agentEnv);
  Slave slave = new DumbSlave(
          name, "CI Docker Agent",
          "ci-agent",
          numExecutors,
          Node.Mode.NORMAL,
          label,
          new JNLPLauncher(),
          new RetentionStrategy.Always(),
          new LinkedList())
  slave.getNodeProperties().add(envPro)
  instance.addNode(slave)

  println "Create " + name + " agent"
}

def numExecs =  System.getenv('JENKINS_AGENT_EXECUTORS') ?: 2
def instance = Jenkins.getInstance()

createSlave(instance, "docker-slave", "docker", numExecs)

instance.save()