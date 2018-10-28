#!groovy

import jenkins.model.*
import hudson.model.*
import hudson.slaves.*
import hudson.plugins.sshslaves.*
import java.util.ArrayList;
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry;

def createSlave(instance, name, label, numExecutors) {
  println "--> Creating [${name}] agent with label [${label}]"

  List<Entry> agentEnv = new ArrayList<Entry>()
  EnvironmentVariablesNodeProperty envPro = new EnvironmentVariablesNodeProperty(agentEnv)
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

  println "Created ${name} agent"
}

def masterExecs = System.getenv("JENKINS_MASTER_EXECUTORS").toInteger() ?: 0
def numExecs = System.getenv('JENKINS_AGENT_EXECUTORS').toInteger() ?: 2
def instance = Jenkins.getInstance()

instance.setNumExecutors(masterExecs)

createSlave(instance, "docker-slave", "docker", numExecs)

instance.save()