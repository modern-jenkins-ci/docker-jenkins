#!groovy

import jenkins.model.*
import hudson.model.*
import hudson.slaves.*
import hudson.plugins.sshslaves.*
import java.util.ArrayList;
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry;

def createSlave(instance, name, label, numExecutors) {
  println "--> Creating [${name}] agent with label [${label}]"

  Slave slave = new DumbSlave(
          name, "CI Docker Agent",
          "ci-agent",
          numExecutors,
          Node.Mode.NORMAL,
          label,
          new JNLPLauncher(),
          new RetentionStrategy.Always(),
          new LinkedList())

  List<Entry> agentEnv = new ArrayList<Entry>()

  if(System.getenv('http_proxy')) {
    println "--> Found http_proxy environment variable, adding to slave env"
    agentEnv.add(new Entry('http_proxy', System.getenv('http_proxy')))
  }

  if(System.getenv('https_proxy')) {
    println "--> Found https_proxy environment variable, adding to slave env"
    agentEnv.add(new Entry('https_proxy', System.getenv('https_proxy')))
  }

  EnvironmentVariablesNodeProperty envPro = new EnvironmentVariablesNodeProperty(agentEnv)
  slave.getNodeProperties().add(envPro)
  instance.addNode(slave)

  println "Created ${name} agent"
}

def masterExecs = System.getenv("JENKINS_MASTER_EXECUTORS").toInteger() ?: 0
def numExecs = System.getenv('JENKINS_AGENT_EXECUTORS').toString() ?: '2'
def instance = Jenkins.getInstance()

instance.setNumExecutors(masterExecs)

createSlave(instance, "docker-agent", "docker", numExecs)

instance.save()